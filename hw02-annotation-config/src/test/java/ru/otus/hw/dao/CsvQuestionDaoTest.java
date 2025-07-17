
package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CsvQuestionDaoTest {

    @Mock
    private TestFileNameProvider fileNameProvider;

    private CsvQuestionDao questionDao;

    @BeforeEach
    void setUp() {
        try (var ignored = MockitoAnnotations.openMocks(this)) {
            questionDao = new CsvQuestionDao(fileNameProvider);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findAll_ShouldReadQuestionsFromValidFile() {
        when(fileNameProvider.getTestFileName()).thenReturn("questions.csv");

        List<Question> questions = questionDao.findAll();

        assertNotNull(questions);
        assertFalse(questions.isEmpty());

        for (Question question : questions) {
            assertNotNull(question.text());
            assertNotNull(question.answers());
            assertFalse(question.answers().isEmpty());
        }

        verify(fileNameProvider).getTestFileName();
    }

    @Test
    void findAll_ShouldThrowExceptionWhenFileNotFound() {
        when(fileNameProvider.getTestFileName()).thenReturn("nonexistent.csv");

        assertThrows(QuestionReadException.class, () -> questionDao.findAll());
        verify(fileNameProvider).getTestFileName();
    }

    @Test
    void findAll_ShouldThrowExceptionWhenFileIsEmpty() {
        when(fileNameProvider.getTestFileName()).thenReturn("empty.csv");

        assertThrows(QuestionReadException.class, () -> questionDao.findAll());
        verify(fileNameProvider).getTestFileName();
    }
}