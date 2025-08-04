package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.QuestionReadException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestRunnerServiceImpl implements TestRunnerService {

    private final TestService testService;

    private final StudentService studentService;

    private final ResultService resultService;

    private final LocalizedIOService ioService;

    @Override
    public void run() {
        var student = studentService.determineCurrentStudent();
        try {
            var testResult = testService.executeTestFor(student);
            resultService.showResult(testResult);
        } catch (QuestionReadException e) {
            ioService.printLineLocalized("TestRunnerService.error.reading");
            log.error("Error reading questions: {}", e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            ioService.printLineLocalized("TestRunnerService.error.input");
            log.error("Input error: {}", e.getMessage(), e);
        } catch (Exception e) {
            ioService.printLineLocalized("TestRunnerService.error.unexpected");
            log.error("Unexpected error during test execution: {}", e.getMessage(), e);
        }
    }
}
