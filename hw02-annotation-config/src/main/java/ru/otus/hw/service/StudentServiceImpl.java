package ru.otus.hw.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Student;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentServiceImpl implements StudentService {

    IOService ioService;

    @Override
    public Student determineCurrentStudent() {
        var firstName = ioService.readStringWithPrompt("Please input your first name");
        var lastName = ioService.readStringWithPrompt("Please input your last name");
        return new Student(firstName, lastName);
    }
}
