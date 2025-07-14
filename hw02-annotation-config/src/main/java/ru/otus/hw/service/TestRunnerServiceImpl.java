package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.exceptions.QuestionReadException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestRunnerServiceImpl implements TestRunnerService {

    private final TestService testService;

    private final StudentService studentService;

    private final ResultService resultService;

    private final IOService ioService;

    @Override
    public void run() {
        var student = studentService.determineCurrentStudent();
        try {
            var testResult = testService.executeTestFor(student);
            resultService.showResult(testResult);
        } catch (QuestionReadException e) {
            ioService.printFormattedLine("Error reading questions: %s", e.getMessage());
            log.error("Error reading questions: {}", e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            ioService.printFormattedLine("Input error: %s", e.getMessage());
            log.error("Input error: {}", e.getMessage(), e);
        } catch (Exception e) {
            ioService.printFormattedLine("Unexpected error occurred: %s", e.getMessage());
            log.error("Unexpected error during test execution: {}", e.getMessage(), e);
        }
    }
}
