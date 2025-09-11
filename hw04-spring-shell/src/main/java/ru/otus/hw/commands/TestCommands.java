package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.ResultService;
import ru.otus.hw.service.StudentService;
import ru.otus.hw.service.TestService;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class TestCommands {

    private final TestService testService;

    private final StudentService studentService;

    private final ResultService resultService;

    private final LocalizedIOService ioService;

    @ShellMethod(value = "Start student testing", key = {"test", "start-test", "run-test"})
    public void runTest() {
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
