package com.library.feature.borrow;

import com.library.domain.model.Book;
import com.library.domain.model.BookHold;
import com.library.domain.model.Student;
import com.library.domain.repository.BookHoldRepository;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.StudentRepository;
import com.library.feature.borrow.HoldRowView;
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
        return bookHoldRepository.countByStudentStudentIdAndStatusIn(studentId, ACTIVE_STATUSES);
    }

    @Transactional
    public BookHold placeHold(Integer studentId, Integer bookId, String note) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y sinh viÃƒÂªn."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y sÃƒÂ¡ch."));

        if (book.getAvailable() != null && book.getAvailable() > 0) {
            throw new IllegalArgumentException("SÃƒÂ¡ch Ã„â€˜ang cÃƒÂ²n sÃ¡ÂºÂµn, bÃ¡ÂºÂ¡n cÃƒÂ³ thÃ¡Â»Æ’ mÃ†Â°Ã¡Â»Â£n trÃ¡Â»Â±c tiÃ¡ÂºÂ¿p.");
        }
        if (bookHoldRepository.existsByStudentStudentIdAndBookBookIdAndStatusIn(studentId, bookId, ACTIVE_STATUSES)) {
            throw new IllegalArgumentException("BÃ¡ÂºÂ¡n Ã„â€˜ÃƒÂ£ cÃƒÂ³ yÃƒÂªu cÃ¡ÂºÂ§u giÃ¡Â»Â¯ chÃ¡Â»â€” Ã„â€˜ang hoÃ¡ÂºÂ¡t Ã„â€˜Ã¡Â»â„¢ng cho sÃƒÂ¡ch nÃƒÂ y.");
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
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y yÃƒÂªu cÃ¡ÂºÂ§u giÃ¡Â»Â¯ chÃ¡Â»â€”."));

        if (!hold.getStudent().getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("BÃ¡ÂºÂ¡n khÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân hÃ¡Â»Â§y yÃƒÂªu cÃ¡ÂºÂ§u nÃƒÂ y.");
        }
        if (!ACTIVE_STATUSES.contains(hold.getStatus())) {
            throw new IllegalArgumentException("YÃƒÂªu cÃ¡ÂºÂ§u nÃƒÂ y khÃƒÂ´ng cÃƒÂ²n Ã¡Â»Å¸ trÃ¡ÂºÂ¡ng thÃƒÂ¡i cÃƒÂ³ thÃ¡Â»Æ’ hÃ¡Â»Â§y.");
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
