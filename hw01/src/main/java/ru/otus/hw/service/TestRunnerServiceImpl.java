package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.otus.hw.exceptions.QuestionReadException;

@Slf4j
@RequiredArgsConstructor
public class TestRunnerServiceImpl implements TestRunnerService {

    private final TestService testService;

    @Override
    public void run() {
        try {
            testService.executeTest();
        } catch (QuestionReadException e) {
            System.out.println("Ошибка при чтении вопросов: " + e.getMessage());
            log.error("Ошибка при чтении вопросов: {}", e.getMessage(), e);
        }
    }
}