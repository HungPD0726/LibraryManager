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
            throw new IllegalArgumentException("Đánh giá phải từ 1 đến 5 sao.");
        }
        if (reviewRepository.existsByBookBookIdAndStudentStudentId(bookId, studentId)) {
            throw new IllegalArgumentException("Bạn đã đánh giá sách này rồi.");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách."));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên."));

        BookReview review = new BookReview();
        review.setBook(book);
        review.setStudent(student);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);
        review.setCreatedDate(LocalDateTime.now());
        return reviewRepository.save(review);
    }
}
