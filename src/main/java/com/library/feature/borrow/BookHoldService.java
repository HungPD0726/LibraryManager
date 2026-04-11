package com.library.feature.borrow;

import com.library.domain.model.Book;
import com.library.domain.model.BookHold;
import com.library.domain.model.Borrow;
import com.library.domain.model.BorrowItem;
import com.library.domain.model.Student;
import com.library.domain.repository.BookHoldRepository;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.StudentRepository;
import com.library.feature.notification.NotificationService;
import com.library.shared.constant.NotificationType;
import com.library.shared.realtime.AdminLiveUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookHoldService {

    static final Set<String> ACTIVE_STATUSES = Set.of("Waiting", "Notified");
    static final String FULFILLED_STATUS = "Fulfilled";
    private static final int AUTO_BORROW_DUE_DAYS = 14;

    private final BookHoldRepository bookHoldRepository;
    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;
    private final BorrowRequestService borrowRequestService;
    private final NotificationService notificationService;
    private final AdminLiveUpdateService adminLiveUpdateService;

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
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y sinh viÃªn."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y sÃ¡ch."));

        if (book.getAvailable() != null && book.getAvailable() > 0) {
            throw new IllegalArgumentException("SÃ¡ch Ä‘ang cÃ²n sáºµn, báº¡n cÃ³ thá»ƒ mÆ°á»£n trá»±c tiáº¿p.");
        }
        if (bookHoldRepository.existsByStudentStudentIdAndBookBookIdAndStatusIn(studentId, bookId, ACTIVE_STATUSES)) {
            throw new IllegalArgumentException("Báº¡n Ä‘Ã£ cÃ³ yÃªu cáº§u giá»¯ chá»— Ä‘ang hoáº¡t Ä‘á»™ng cho sÃ¡ch nÃ y.");
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
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y yÃªu cáº§u giá»¯ chá»—."));

        if (!hold.getStudent().getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("Báº¡n khÃ´ng cÃ³ quyá»n há»§y yÃªu cáº§u nÃ y.");
        }
        if (!ACTIVE_STATUSES.contains(hold.getStatus())) {
            throw new IllegalArgumentException("YÃªu cáº§u nÃ y khÃ´ng cÃ²n á»Ÿ tráº¡ng thÃ¡i cÃ³ thá»ƒ há»§y.");
        }

        hold.setStatus("Cancelled");
        hold.setExpireDate(LocalDateTime.now());
        bookHoldRepository.save(hold);
    }

    @Transactional
    public List<Borrow> fulfillAvailableHoldsForReturnedItems(Integer staffId, List<BorrowItem> returnedItems) {
        if (staffId == null || returnedItems == null || returnedItems.isEmpty()) {
            return List.of();
        }

        List<Borrow> autoBorrows = new ArrayList<>();
        for (BorrowItem returnedItem : returnedItems) {
            if (returnedItem == null || returnedItem.getBookId() == null) {
                continue;
            }

            Book book = resolveBook(returnedItem);
            int availableCopies = book.getAvailable() == null ? 0 : Math.max(book.getAvailable(), 0);
            if (availableCopies <= 0) {
                continue;
            }

            List<BookHold> queuedHolds = bookHoldRepository.findByBookBookIdAndStatusInOrderByHoldDateAsc(
                    book.getBookId(),
                    ACTIVE_STATUSES
            );

            int allocations = Math.min(availableCopies, queuedHolds.size());
            for (int index = 0; index < allocations; index++) {
                BookHold hold = queuedHolds.get(index);
                Borrow autoBorrow = createBorrowFromHold(hold, staffId);
                markHoldFulfilled(hold);
                sendHoldFulfilledNotification(hold, autoBorrow, book);
                adminLiveUpdateService.publishHoldFulfilled(hold, autoBorrow);
                autoBorrows.add(autoBorrow);
            }
        }

        return autoBorrows;
    }

    private Borrow createBorrowFromHold(BookHold hold, Integer staffId) {
        BorrowItem borrowItem = new BorrowItem();
        borrowItem.setBookId(hold.getBook().getBookId());
        borrowItem.setQuantity(1);

        return borrowRequestService.createBorrow(
                hold.getStudent().getStudentId(),
                staffId,
                List.of(borrowItem),
                LocalDate.now().plusDays(AUTO_BORROW_DUE_DAYS)
        );
    }

    private void markHoldFulfilled(BookHold hold) {
        LocalDateTime now = LocalDateTime.now();
        hold.setStatus(FULFILLED_STATUS);
        hold.setNotifiedDate(hold.getNotifiedDate() != null ? hold.getNotifiedDate() : now);
        hold.setExpireDate(now);
        bookHoldRepository.save(hold);
    }

    private void sendHoldFulfilledNotification(BookHold hold, Borrow autoBorrow, Book book) {
        notificationService.create(
                hold.getStudent().getStudentId(),
                "Giá»¯ chá»— Ä‘Ã£ Ä‘Æ°á»£c xá»­ lÃ½",
                "SÃ¡ch " + book.getBookName() + " Ä‘Ã£ Ä‘Æ°á»£c chuyá»ƒn thÃ nh phiáº¿u mÆ°á»£n #"
                        + autoBorrow.getBorrowId() + " cho báº¡n.",
                NotificationType.HOLD_FULFILLED
        );
    }

    private Book resolveBook(BorrowItem returnedItem) {
        if (returnedItem.getBook() != null) {
            return returnedItem.getBook();
        }
        return bookRepository.findById(returnedItem.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y sÃ¡ch."));
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
