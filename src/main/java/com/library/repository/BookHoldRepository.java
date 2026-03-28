package com.library.repository;

import com.library.entity.BookHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BookHoldRepository extends JpaRepository<BookHold, Integer> {

    List<BookHold> findByStudentStudentIdAndStatusInOrderByHoldDateDesc(Integer studentId, Collection<String> statuses);

    boolean existsByStudentStudentIdAndBookBookIdAndStatusIn(Integer studentId, Integer bookId, Collection<String> statuses);
}
