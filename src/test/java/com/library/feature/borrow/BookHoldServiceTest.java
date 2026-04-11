package com.library.feature.borrow;

import com.library.domain.model.Book;
import com.library.domain.model.BookHold;
import com.library.domain.model.Borrow;
import com.library.domain.model.BorrowItem;
import com.library.domain.model.Notification;
import com.library.domain.model.Student;
import com.library.domain.repository.BookHoldRepository;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.StudentRepository;
import com.library.feature.notification.NotificationService;
import com.library.shared.constant.NotificationType;
import com.library.shared.realtime.AdminLiveUpdateService;
import com.library.shared.support.NotificationTextSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookHoldServiceTest {

    @Mock
    private BookHoldRepository bookHoldRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private StudentRepository studentRepository;

    private RecordingBorrowRequestService borrowRequestService;
    private RecordingNotificationService notificationService;
    private RecordingAdminLiveUpdateService adminLiveUpdateService;
    private BookHoldService bookHoldService;

    @BeforeEach
    void setUp() {
        borrowRequestService = new RecordingBorrowRequestService();
        notificationService = new RecordingNotificationService();
        adminLiveUpdateService = new RecordingAdminLiveUpdateService();
        bookHoldService = new BookHoldService(
                bookHoldRepository,
                bookRepository,
                studentRepository,
                borrowRequestService,
                notificationService,
                adminLiveUpdateService
        );
    }

    @Test
    void placeHold_shouldRejectWhenBookIsStillAvailable() {
        Student student = new Student();
        student.setStudentId(7);

        Book book = new Book();
        book.setBookId(10);
        book.setAvailable(2);

        when(studentRepository.findById(7)).thenReturn(Optional.of(student));
        when(bookRepository.findById(10)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookHoldService.placeHold(7, 10, "Need it soon"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SÃ¡ch Ä‘ang cÃ²n sáºµn, báº¡n cÃ³ thá»ƒ mÆ°á»£n trá»±c tiáº¿p.");

        verify(bookHoldRepository, never()).save(any(BookHold.class));
    }

    @Test
    void placeHold_shouldRejectWhenStudentAlreadyHasActiveHold() {
        Student student = new Student();
        student.setStudentId(7);

        Book book = new Book();
        book.setBookId(10);
        book.setAvailable(0);

        when(studentRepository.findById(7)).thenReturn(Optional.of(student));
        when(bookRepository.findById(10)).thenReturn(Optional.of(book));
        when(bookHoldRepository.existsByStudentStudentIdAndBookBookIdAndStatusIn(any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> bookHoldService.placeHold(7, 10, "Need it soon"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Báº¡n Ä‘Ã£ cÃ³ yÃªu cáº§u giá»¯ chá»— Ä‘ang hoáº¡t Ä‘á»™ng cho sÃ¡ch nÃ y.");

        verify(bookHoldRepository, never()).save(any(BookHold.class));
    }

    @Test
    void placeHold_shouldPersistWaitingHoldWhenBookIsUnavailable() {
        Student student = new Student();
        student.setStudentId(7);

        Book book = new Book();
        book.setBookId(10);
        book.setBookName("Clean Architecture");
        book.setAvailable(0);

        when(studentRepository.findById(7)).thenReturn(Optional.of(student));
        when(bookRepository.findById(10)).thenReturn(Optional.of(book));
        when(bookHoldRepository.existsByStudentStudentIdAndBookBookIdAndStatusIn(any(), any(), any()))
                .thenReturn(false);
        when(bookHoldRepository.save(any(BookHold.class))).thenAnswer(invocation -> {
            BookHold hold = invocation.getArgument(0);
            hold.setHoldId(15);
            return hold;
        });

        BookHold created = bookHoldService.placeHold(7, 10, "Reserve for next week");

        assertThat(created.getHoldId()).isEqualTo(15);
        assertThat(created.getStudent()).isSameAs(student);
        assertThat(created.getBook()).isSameAs(book);
        assertThat(created.getStatus()).isEqualTo("Waiting");
        assertThat(created.getNote()).isEqualTo("Reserve for next week");
        assertThat(created.getHoldDate()).isNotNull();
    }

    @Test
    void cancelHold_shouldRejectWhenHoldBelongsToAnotherStudent() {
        Student owner = new Student();
        owner.setStudentId(9);

        BookHold hold = new BookHold();
        hold.setHoldId(21);
        hold.setStudent(owner);
        hold.setStatus("Waiting");

        when(bookHoldRepository.findById(21)).thenReturn(Optional.of(hold));

        assertThatThrownBy(() -> bookHoldService.cancelHold(7, 21))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Báº¡n khÃ´ng cÃ³ quyá»n há»§y yÃªu cáº§u nÃ y.");
    }

    @Test
    void cancelHold_shouldRejectWhenStatusIsNotActive() {
        Student owner = new Student();
        owner.setStudentId(7);

        BookHold hold = new BookHold();
        hold.setHoldId(21);
        hold.setStudent(owner);
        hold.setStatus("Cancelled");

        when(bookHoldRepository.findById(21)).thenReturn(Optional.of(hold));

        assertThatThrownBy(() -> bookHoldService.cancelHold(7, 21))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("YÃªu cáº§u nÃ y khÃ´ng cÃ²n á»Ÿ tráº¡ng thÃ¡i cÃ³ thá»ƒ há»§y.");
    }

    @Test
    void cancelHold_shouldMarkHoldCancelledAndSetExpireDate() {
        Student owner = new Student();
        owner.setStudentId(7);

        BookHold hold = new BookHold();
        hold.setHoldId(21);
        hold.setStudent(owner);
        hold.setStatus("Notified");

        when(bookHoldRepository.findById(21)).thenReturn(Optional.of(hold));
        when(bookHoldRepository.save(any(BookHold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookHoldService.cancelHold(7, 21);

        ArgumentCaptor<BookHold> holdCaptor = ArgumentCaptor.forClass(BookHold.class);
        verify(bookHoldRepository).save(holdCaptor.capture());
        BookHold saved = holdCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo("Cancelled");
        assertThat(saved.getExpireDate()).isNotNull();
    }

    @Test
    void fulfillAvailableHoldsForReturnedItems_shouldCreateBorrowAndCloseHold() {
        Student student = new Student();
        student.setStudentId(7);
        student.setStudentName("Nguyen Minh");

        Book book = new Book();
        book.setBookId(10);
        book.setBookName("Clean Architecture");
        book.setAvailable(1);

        BookHold hold = new BookHold();
        hold.setHoldId(21);
        hold.setStudent(student);
        hold.setBook(book);
        hold.setStatus("Waiting");

        BorrowItem returnedItem = new BorrowItem();
        returnedItem.setBookId(10);
        returnedItem.setQuantity(1);
        returnedItem.setBook(book);

        when(bookHoldRepository.findByBookBookIdAndStatusInOrderByHoldDateAsc(eq(10), anyCollection()))
                .thenReturn(List.of(hold));
        when(bookHoldRepository.save(any(BookHold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Borrow> autoBorrows = bookHoldService.fulfillAvailableHoldsForReturnedItems(4, List.of(returnedItem));

        assertThat(autoBorrows).hasSize(1);
        assertThat(autoBorrows.get(0).getBorrowId()).isEqualTo(91);
        assertThat(hold.getStatus()).isEqualTo(BookHoldService.FULFILLED_STATUS);
        assertThat(hold.getExpireDate()).isNotNull();
        assertThat(hold.getNotifiedDate()).isNotNull();
        assertThat(borrowRequestService.lastStudentId).isEqualTo(7);
        assertThat(borrowRequestService.lastStaffId).isEqualTo(4);
        assertThat(borrowRequestService.lastBookId).isEqualTo(10);
        assertThat(borrowRequestService.lastDueDate).isEqualTo(LocalDate.now().plusDays(14));
        assertThat(notificationService.lastStudentId).isEqualTo(7);
        assertThat(notificationService.lastTitle).isEqualTo("Giá»¯ chá»— Ä‘Ã£ Ä‘Æ°á»£c xá»­ lÃ½");
        assertThat(notificationService.lastMessage).isEqualTo("SÃ¡ch Clean Architecture Ä‘Ã£ Ä‘Æ°á»£c chuyá»ƒn thÃ nh phiáº¿u mÆ°á»£n #91 cho báº¡n.");
        assertThat(notificationService.lastType).isEqualTo(NotificationType.HOLD_FULFILLED);
        assertThat(adminLiveUpdateService.lastHold).isSameAs(hold);
        assertThat(adminLiveUpdateService.lastBorrow).isSameAs(autoBorrows.get(0));
    }

    @Test
    void countActiveByStudent_shouldUseRepositoryCountQuery() {
        when(bookHoldRepository.countByStudentStudentIdAndStatusIn(anyInt(), anyCollection())).thenReturn(3L);

        long count = bookHoldService.countActiveByStudent(7);

        assertThat(count).isEqualTo(3);
        verify(bookHoldRepository, never()).findByStudentStudentIdAndStatusInOrderByHoldDateDesc(anyInt(), anyCollection());
    }

    private static final class RecordingBorrowRequestService extends BorrowRequestService {

        private Integer lastStudentId;
        private Integer lastStaffId;
        private Integer lastBookId;
        private LocalDate lastDueDate;

        private RecordingBorrowRequestService() {
            super(null, null, null, null, null);
        }

        @Override
        public Borrow createBorrow(Integer studentId, Integer staffId, List<BorrowItem> items, LocalDate dueDate) {
            this.lastStudentId = studentId;
            this.lastStaffId = staffId;
            this.lastBookId = items.get(0).getBookId();
            this.lastDueDate = dueDate;

            Borrow borrow = new Borrow();
            borrow.setBorrowId(91);
            return borrow;
        }
    }

    private static final class RecordingNotificationService extends NotificationService {

        private Integer lastStudentId;
        private String lastTitle;
        private String lastMessage;
        private String lastType;

        private RecordingNotificationService() {
            super(null, null, new NotificationTextSupport());
        }

        @Override
        public Notification create(Integer studentId, String title, String message, String type) {
            this.lastStudentId = studentId;
            this.lastTitle = title;
            this.lastMessage = message;
            this.lastType = type;
            return new Notification();
        }
    }

    private static final class RecordingAdminLiveUpdateService extends AdminLiveUpdateService {

        private BookHold lastHold;
        private Borrow lastBorrow;

        private RecordingAdminLiveUpdateService() {
            super(null, null);
        }

        @Override
        public void publishHoldFulfilled(BookHold hold, Borrow autoBorrow) {
            this.lastHold = hold;
            this.lastBorrow = autoBorrow;
        }
    }
}
