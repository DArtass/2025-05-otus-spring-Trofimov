package ru.otus.hw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.CsvQuestionDao;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.service.IOService;
import ru.otus.hw.service.TestServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CommonHwTest {

    private static final String CONFIGURATION_ANNOTATION_NAME = "org.springframework.context.annotation.Configuration";

    @Test
    void shouldNotContainConfigurationAnnotationAboveItSelf() {
        assertThat(AppProperties.class.isAnnotationPresent(Configuration.class))
                .withFailMessage("Класс свойств не является конфигурацией т.к. " +
                        "конфигурация для создания бинов, а тут просто компонент группирующий свойства приложения")
                .isFalse();
    }

    @Test
    void shouldNotContainPropertySourceAnnotationAboveItSelf() {
        assertThat(AppProperties.class.isAnnotationPresent(PropertySource.class))
                .withFailMessage("Аннотацию @PropertySource лучше вешать над конфигурацией, " +
                        "а класс свойств ей не является")
                .isFalse();
    }

    @Test
    void shouldNotContainFieldInjectedDependenciesOrProperties() {
        var provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter((mr, mf) -> {
            var metaData = mr.getClassMetadata();
            var annotationMetaData = mr.getAnnotationMetadata();
            var isTest = metaData.getClassName().endsWith("Test");
            var isInterface = metaData.isInterface();
            var isConfiguration = annotationMetaData.hasAnnotation(CONFIGURATION_ANNOTATION_NAME);
            var clazz = getBeanClassByName(metaData.getClassName());
            var classContainsFieldInjectedDependenciesOrProperties = Arrays.stream(clazz.getDeclaredFields())
                    .anyMatch(f -> f.isAnnotationPresent(Autowired.class) || f.isAnnotationPresent(Value.class));
            return !isTest && !isInterface && !isConfiguration && classContainsFieldInjectedDependenciesOrProperties;
        });

        var classesContainsFieldInjectedDependenciesOrProperties =
                provider.findCandidateComponents(Application.class.getPackageName());

        var classesNames = classesContainsFieldInjectedDependenciesOrProperties.stream()
                .map(BeanDefinition::getBeanClassName).collect(Collectors.joining("%n"));
        assertThat(classesContainsFieldInjectedDependenciesOrProperties)
                .withFailMessage("На курсе все внедрение рекомендовано осуществлять через конструктор (" +
                        "в т.ч. @Value). Следующие классы нарушают это правило: %n%s".formatted(classesNames))
                .isEmpty();
    }

    @Test
    public void testExecuteTest() {
        var student = new Student("John", "Doe");
        var answers1 = List.of(
                new Answer("Wrong1", false),
                new Answer("Correct1", true),
                new Answer("Wrong2", false)
        );
        var answers2 = List.of(
                new Answer("Correct2", true),
                new Answer("Wrong3", false)
        );
        var questions = List.of(
                new Question("Test Question 1", answers1),
                new Question("Test Question 2", answers2)
        );

        var mockQuestionDao = mock(QuestionDao.class);
        var mockIOService = mock(IOService.class);

        when(mockQuestionDao.findAll()).thenReturn(questions);
        when(mockIOService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(2)
                .thenReturn(1);

        var testService = new TestServiceImpl(mockIOService, mockQuestionDao);

        var result = testService.executeTestFor(student);

        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getRightAnswersCount()).isEqualTo(2);
        assertThat(result.getAnsweredQuestions()).hasSize(2);

        verify(mockIOService, times(3)).printLine(""); // 1 в начале + 2 для каждого вопроса
        verify(mockIOService).printFormattedLine("Please answer the questions below%n");
        verify(mockIOService).printLine("Test Question 1");
        verify(mockIOService).printLine("Test Question 2");
        verify(mockIOService).printFormattedLine("%d. %s", 1, "Wrong1");
        verify(mockIOService).printFormattedLine("%d. %s", 2, "Correct1");
        verify(mockIOService).printFormattedLine("%d. %s", 3, "Wrong2");
        verify(mockIOService).printFormattedLine("%d. %s", 1, "Correct2");
        verify(mockIOService).printFormattedLine("%d. %s", 2, "Wrong3");
        verify(mockQuestionDao).findAll();
    }

    @Test
    void shouldReadQuestionsFromCsvFile() {
        var mockTestFileNameProvider = mock(TestFileNameProvider.class);
        when(mockTestFileNameProvider.getTestFileName()).thenReturn("questions.csv");

        var csvQuestionDao = new CsvQuestionDao(mockTestFileNameProvider);

        var questions = csvQuestionDao.findAll();

        assertThat(questions).isNotEmpty();
        assertThat(questions).hasSize(4);

        var firstQuestion = questions.get(0);
        assertThat(firstQuestion.text()).isNotEmpty();
        assertThat(firstQuestion.answers()).isNotEmpty();

        for (var question : questions) {
            assertThat(question.answers())
                    .withFailMessage("Вопрос '%s' должен иметь хотя бы один правильный ответ", question.text())
                    .anyMatch(Answer::isCorrect);
        }

        for (var question : questions) {
            assertThat(question.answers())
                    .withFailMessage("Вопрос '%s' должен иметь хотя бы один неправильный ответ", question.text())
                    .anyMatch(answer -> !answer.isCorrect());
        }

        for (var question : questions) {
            for (var answer : question.answers()) {
                assertThat(answer.text())
                        .withFailMessage("Ответ не должен быть пустым")
                        .isNotBlank();
            }
        }

        verify(mockTestFileNameProvider).getTestFileName();
    }

    @Test
    void shouldThrowExceptionWhenFileNotFound() {
        var mockTestFileNameProvider = mock(TestFileNameProvider.class);
        when(mockTestFileNameProvider.getTestFileName()).thenReturn("nonexistent.csv");

        var csvQuestionDao = new CsvQuestionDao(mockTestFileNameProvider);

        assertThatThrownBy(csvQuestionDao::findAll)
                .isInstanceOf(QuestionReadException.class)
                .hasMessage("Error reading questions from CSV file")
                .hasCauseInstanceOf(NullPointerException.class);

        verify(mockTestFileNameProvider).getTestFileName();
    }

    private Class<?> getBeanClassByName(String beanClassName) {
        try {
            return Class.forName(beanClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}