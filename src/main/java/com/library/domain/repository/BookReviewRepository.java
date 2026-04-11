package com.library.domain.repository;

import com.library.domain.model.BookReview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, Integer> {

    @EntityGraph(attributePaths = {"student"})
    List<BookReview> findByBookBookIdOrderByCreatedDateDesc(Integer bookId);

    List<BookReview> findByStudentStudentIdOrderByCreatedDateDesc(Integer studentId);

    boolean existsByBookBookIdAndStudentStudentId(Integer bookId, Integer studentId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM BookReview r WHERE r.book.bookId = :bookId")
    Double averageRatingByBookId(@Param("bookId") Integer bookId);

    @Query("SELECT COUNT(r) FROM BookReview r WHERE r.book.bookId = :bookId")
    long countByBookId(@Param("bookId") Integer bookId);
}
