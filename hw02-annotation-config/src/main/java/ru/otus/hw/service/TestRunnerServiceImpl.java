package ru.otus.hw.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.QuestionReadException;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestRunnerServiceImpl implements TestRunnerService {

    TestService testService;

    StudentService studentService;

    ResultService resultService;

    @Override
    public void run() {
        var student = studentService.determineCurrentStudent();
        try {
            var testResult = testService.executeTestFor(student);
            resultService.showResult(testResult);
        } catch (QuestionReadException e) {
            System.out.println("Ошибка при чтении вопросов: " + e.getMessage());
            log.error("Ошибка при чтении вопросов: {}", e.getMessage(), e);
        }
    }
}
