package com.group6.librarymanager.model.dao;

import com.group6.librarymanager.model.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentDAO extends JpaRepository<Student, Integer> {

    Optional<Student> findByEmail(String email);

    Optional<Student> findByStudentName(String studentName);
}
