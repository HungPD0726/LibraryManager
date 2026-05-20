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
import com.library.shared.constant.HoldStatus;
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

    static final Set<String> ACTIVE_STATUSES = Set.of(HoldStatus.WAITING, HoldStatus.NOTIFIED);
    static final String FULFILLED_STATUS = HoldStatus.FULFILLED;
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
        hold.setStatus(HoldStatus.WAITING);
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

        hold.setStatus(HoldStatus.CANCELLED);
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
        hold.setStatus(HoldStatus.FULFILLED);
        hold.setNotifiedDate(hold.getNotifiedDate() != null ? hold.getNotifiedDate() : now);
        hold.setExpireDate(now);
        bookHoldRepository.save(hold);
    }

    private void sendHoldFulfilledNotification(BookHold hold, Borrow autoBorrow, Book book) {
        notificationService.create(
                hold.getStudent().getStudentId(),
                "Giữ chỗ đã được xử lý",
                "Sách " + book.getBookName() + " đã được chuyển thành phiếu mượn #"
                        + autoBorrow.getBorrowId() + " cho bạn.",
                NotificationType.HOLD_FULFILLED
        );
    }

    private Book resolveBook(BorrowItem returnedItem) {
        if (returnedItem.getBook() != null) {
            return returnedItem.getBook();
        }
        return bookRepository.findById(returnedItem.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách."));
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
