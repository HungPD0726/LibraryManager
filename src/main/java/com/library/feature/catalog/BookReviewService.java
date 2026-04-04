package com.library.feature.catalog;

import com.library.domain.model.Book;
import com.library.domain.model.BookReview;
import com.library.domain.model.Student;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.BookReviewRepository;
import com.library.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookReviewService {

    private final BookReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public List<BookReview> findByBookId(Integer bookId) {
        return reviewRepository.findByBookBookIdOrderByCreatedDateDesc(bookId);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Integer bookId) {
        return reviewRepository.averageRatingByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public long getReviewCount(Integer bookId) {
        return reviewRepository.countByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public boolean hasReviewed(Integer bookId, Integer studentId) {
        return reviewRepository.existsByBookBookIdAndStudentStudentId(bookId, studentId);
    }

    @Transactional
    public BookReview addReview(Integer bookId, Integer studentId, Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Ã„ÂÃƒÂ¡nh giÃƒÂ¡ phÃ¡ÂºÂ£i tÃ¡Â»Â« 1 Ã„â€˜Ã¡ÂºÂ¿n 5 sao.");
        }
        if (reviewRepository.existsByBookBookIdAndStudentStudentId(bookId, studentId)) {
            throw new IllegalArgumentException("BÃ¡ÂºÂ¡n Ã„â€˜ÃƒÂ£ Ã„â€˜ÃƒÂ¡nh giÃƒÂ¡ sÃƒÂ¡ch nÃƒÂ y rÃ¡Â»â€œi.");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y sÃƒÂ¡ch."));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y sinh viÃƒÂªn."));

        BookReview review = new BookReview();
        review.setBook(book);
        review.setStudent(student);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);
        review.setCreatedDate(LocalDateTime.now());
        return reviewRepository.save(review);
    }
}
