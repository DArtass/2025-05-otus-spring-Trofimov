package ru.otus.hw.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestResult {
    Student student;

    List<Question> answeredQuestions;

    @NonFinal
    int rightAnswersCount;

    public TestResult(Student student) {
        this.student = student;
        this.answeredQuestions = new ArrayList<>();
    }

    public void applyAnswer(Question question, boolean isRightAnswer) {
        answeredQuestions.add(question);
        if (isRightAnswer) {
            rightAnswersCount++;
        }
    }
}
