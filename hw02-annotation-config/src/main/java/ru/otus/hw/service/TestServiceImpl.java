package ru.otus.hw.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestServiceImpl implements TestService {

    IOService ioService;

    QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question: questions) {
            var isAnswerValid = askQuestion(question);
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }

    private boolean askQuestion(Question question) {
        ioService.printLine("");
        ioService.printLine(question.text());

        var answers = question.answers();
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("%d. %s", i + 1, answers.get(i).text());
        }

        int userAnswerIndex = ioService.readIntForRangeWithPrompt(
                1,
                answers.size(),
                "Your answer: ",
                "Please enter a number from 1 to " + answers.size()
        ) - 1;

        return answers.get(userAnswerIndex).isCorrect();
    }
}
