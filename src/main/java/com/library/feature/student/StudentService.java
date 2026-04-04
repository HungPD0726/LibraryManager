package com.library.feature.student;

import com.library.domain.model.Student;
import com.library.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public Page<Student> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("studentId").descending());
        return studentRepository.findAll(pageable);
    }

    public Page<Student> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("studentId").descending());
        return studentRepository.search(keyword, pageable);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findById(Integer id) {
        return studentRepository.findById(id);
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public void deleteById(Integer id) {
        studentRepository.deleteById(id);
    }

    public long count() {
        return studentRepository.count();
    }
}
