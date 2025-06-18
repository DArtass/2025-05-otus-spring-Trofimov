package ru.otus.hw.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppProperties implements TestConfig, TestFileNameProvider {

    int rightAnswersCountToPass;

    String testFileName;

    public AppProperties(@Value("${test.rightAnswersCountToPass}") int rightAnswersCountToPass,
                         @Value("${test.fileName}") String testFileName) {
        this.rightAnswersCountToPass = rightAnswersCountToPass;
        this.testFileName = testFileName;
    }
}
