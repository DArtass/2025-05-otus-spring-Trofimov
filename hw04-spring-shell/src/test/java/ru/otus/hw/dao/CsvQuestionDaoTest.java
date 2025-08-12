package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {"spring.shell.interactive.enabled=false"})
class CsvQuestionDaoTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public TestFileNameProvider testFileNameProvider() {
            return mock(TestFileNameProvider.class);
        }
    }

    @Autowired
    private TestFileNameProvider fileNameProvider;

    @Autowired
    private CsvQuestionDao questionDao;

    @BeforeEach
    void setUp() {
        reset(fileNameProvider);
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