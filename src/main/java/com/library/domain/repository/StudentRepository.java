package com.library.domain.repository;

import com.library.domain.model.Student;
import com.library.feature.student.StudentOptionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByEmail(String email);

    @Query(
        "SELECT s FROM Student s WHERE LOWER(s.studentName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    org.springframework.data.domain.Page<Student> search(
        @org.springframework.data.repository.query.Param("keyword") String keyword,
        org.springframework.data.domain.Pageable pageable);

    @Query("""
            SELECT new com.library.feature.student.StudentOptionView(s.studentId, s.studentName, s.email)
            FROM Student s
            ORDER BY s.studentName, s.studentId
            """)
    List<StudentOptionView> findAllOptions();
}
