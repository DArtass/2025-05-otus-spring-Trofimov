package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class TestServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    private TestServiceImpl testService;

    @BeforeEach
    void setUp() {
        try (var ignored = MockitoAnnotations.openMocks(this)) {
            testService = new TestServiceImpl(ioService, questionDao);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void executeTestFor_ShouldReturnCorrectTestResult() {
        var student = new Student("John", "Doe");
        var answer1 = new Answer("Answer 1", true);
        var answer2 = new Answer("Answer 2", false);
        var question = new Question("Test question?", List.of(answer1, answer2));
        var questions = List.of(question);

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString())).thenReturn(1);

        TestResult result = testService.executeTestFor(student);

        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(1, result.getAnsweredQuestions().size());
        assertEquals(1, result.getRightAnswersCount());

        verify(questionDao).findAll();
        verify(ioService).printFormattedLine("Please answer the questions below%n");
        verify(ioService).readIntForRangeWithPrompt(eq(1), eq(2), anyString(), anyString());
    }

    @Test
    void executeTestFor_ShouldHandleWrongAnswers() {
        var student = new Student("Jane", "Smith");
        var answer1 = new Answer("Answer 1", true);
        var answer2 = new Answer("Answer 2", false);
        var question = new Question("Test question?", List.of(answer1, answer2));
        var questions = List.of(question);

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString())).thenReturn(2);

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
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1)
                .thenReturn(1);

        TestResult result = testService.executeTestFor(student);

        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(2, result.getAnsweredQuestions().size());
        assertEquals(1, result.getRightAnswersCount());
    }
}