package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {"spring.shell.interactive.enabled=false"})
class TestServiceImplTest {

    @MockitoBean
    private LocalizedIOService ioService;
    @MockitoBean
    private QuestionDao questionDao;
    @Autowired
    private TestServiceImpl testService;

    @BeforeEach
    void setUp() {
        reset(ioService, questionDao);
    }

    @Test
    void executeTestFor_ShouldReturnCorrectTestResult() {
        var student = new Student("John", "Doe");
        var answer1 = new Answer("Answer 1", true);
        var answer2 = new Answer("Answer 2", false);
        var question = new Question("Test question?", List.of(answer1, answer2));
        var questions = List.of(question);

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString())).thenReturn(1);

        TestResult result = testService.executeTestFor(student);

        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(1, result.getAnsweredQuestions().size());
        assertEquals(1, result.getRightAnswersCount());

        verify(questionDao).findAll();
        verify(ioService).printLineLocalized("TestService.answer.the.questions");
        verify(ioService, times(3)).printLine("");
        verify(ioService).printFormattedLineLocalized("TestService.question.prompt", "Test question?");
        verify(ioService).printFormattedLine("%d. %s", 1, "Answer 1");
        verify(ioService).printFormattedLine("%d. %s", 2, "Answer 2");
        verify(ioService).readIntForRangeWithPromptLocalized(eq(1), eq(2), eq("TestService.answer.prompt"), eq("TestService.answer.error"));
    }

    @Test
    void executeTestFor_ShouldHandleWrongAnswers() {
        var student = new Student("Jane", "Smith");
        var answer1 = new Answer("Answer 1", true);
        var answer2 = new Answer("Answer 2", false);
        var question = new Question("Test question?", List.of(answer1, answer2));
        var questions = List.of(question);

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString())).thenReturn(2);

        TestResult result = testService.executeTestFor(student);

        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(1, result.getAnsweredQuestions().size());
        assertEquals(0, result.getRightAnswersCount());
    }

    @Test
    void executeTestFor_ShouldHandleMultipleQuestions() {
        var student = new Student("Bob", "Johnson");
        var question1 = new Question("Question 1?", List.of(
                new Answer("Answer 1", true),
                new Answer("Answer 2", false)
        ));
        var question2 = new Question("Question 2?", List.of(
                new Answer("Answer 1", false),
                new Answer("Answer 2", true)
        ));
        var questions = List.of(question1, question2);

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1)
                .thenReturn(1);

        TestResult result = testService.executeTestFor(student);

        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(2, result.getAnsweredQuestions().size());
        assertEquals(1, result.getRightAnswersCount());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public LocalizedIOService localizedIOService() {
            return mock(LocalizedIOService.class);
        }

        @Bean
        @Primary
        public QuestionDao questionDao() {
            return mock(QuestionDao.class);
        }

        @Bean
        @Primary
        public TestServiceImpl testService(LocalizedIOService ioService, QuestionDao questionDao) {
            return new TestServiceImpl(ioService, questionDao);
        }
    }
}