package ru.otus.hw.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CsvQuestionDao.class)
@TestPropertySource(properties = {"spring.shell.interactive.enabled=false"})
class CsvQuestionDaoTest {

    @MockitoBean
    private TestFileNameProvider fileNameProvider;

    @Autowired
    private CsvQuestionDao questionDao;

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