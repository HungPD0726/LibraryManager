package com.library.service;

import com.library.entity.Book;
import com.library.entity.BookHold;
import com.library.entity.Student;
import com.library.repository.BookHoldRepository;
import com.library.repository.BookRepository;
import com.library.repository.StudentRepository;
import com.library.web.view.HoldRowView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookHoldService {

    private static final Set<String> ACTIVE_STATUSES = Set.of("Waiting", "Notified");

    private final BookHoldRepository bookHoldRepository;
    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public List<HoldRowView> findActiveByStudent(Integer studentId) {
        return bookHoldRepository.findByStudentStudentIdAndStatusInOrderByHoldDateDesc(studentId, ACTIVE_STATUSES).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countActiveByStudent(Integer studentId) {
        return findActiveByStudent(studentId).size();
    }

    @Transactional
    public BookHold placeHold(Integer studentId, Integer bookId, String note) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách."));

        if (book.getAvailable() != null && book.getAvailable() > 0) {
            throw new IllegalArgumentException("Sách đang còn sẵn, bạn có thể mượn trực tiếp.");
        }
        if (bookHoldRepository.existsByStudentStudentIdAndBookBookIdAndStatusIn(studentId, bookId, ACTIVE_STATUSES)) {
            throw new IllegalArgumentException("Bạn đã có yêu cầu giữ chỗ đang hoạt động cho sách này.");
        }

        BookHold hold = new BookHold();
        hold.setStudent(student);
        hold.setBook(book);
        hold.setStatus("Waiting");
        hold.setNote(note);
        hold.setHoldDate(LocalDateTime.now());
        return bookHoldRepository.save(hold);
    }

    @Transactional
    public void cancelHold(Integer studentId, Integer holdId) {
        BookHold hold = bookHoldRepository.findById(holdId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu giữ chỗ."));

        if (!hold.getStudent().getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy yêu cầu này.");
        }
        if (!ACTIVE_STATUSES.contains(hold.getStatus())) {
            throw new IllegalArgumentException("Yêu cầu này không còn ở trạng thái có thể hủy.");
        }

        hold.setStatus("Cancelled");
        hold.setExpireDate(LocalDateTime.now());
        bookHoldRepository.save(hold);
    }

    private HoldRowView toView(BookHold hold) {
        return new HoldRowView(
                hold.getHoldId(),
                hold.getBook().getBookId(),
                hold.getBook().getBookName(),
                hold.getStatus(),
                hold.getHoldDate(),
                hold.getExpireDate(),
                hold.getNote()
        );
    }
}
