package com.library.service;

import com.library.constant.BorrowStatus;
import com.library.entity.Book;
import com.library.entity.Borrow;
import com.library.entity.BorrowItem;
import com.library.entity.Staff;
import com.library.entity.Student;
import com.library.repository.BookRepository;
import com.library.repository.BorrowItemRepository;
import com.library.repository.BorrowRepository;
import com.library.repository.StaffRepository;
import com.library.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRepository borrowRepository;
    @Mock
    private BorrowItemRepository borrowItemRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private BorrowService borrowService;

    @Test
    void createBorrow_directBorrowShouldDecreaseAvailabilityImmediately() {
        Student student = new Student();
        student.setStudentId(7);
        Staff staff = new Staff();
        staff.setStaffId(3);
        Book book = new Book();
        book.setBookId(10);
        book.setBookName("Clean Code");
        book.setAvailable(4);

        BorrowItem item = new BorrowItem();
        item.setBookId(10);
        item.setQuantity(2);

        when(studentRepository.findById(7)).thenReturn(Optional.of(student));
        when(staffRepository.findById(3)).thenReturn(Optional.of(staff));
        when(bookRepository.findById(10)).thenReturn(Optional.of(book));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> {
            Borrow borrow = invocation.getArgument(0);
            if (borrow.getBorrowId() == null) {
                borrow.setBorrowId(99);
            }
            return borrow;
        });

        Borrow created = borrowService.createBorrow(7, 3, List.of(item), LocalDate.now().plusDays(7));

        assertThat(created.getStatus()).isEqualTo(BorrowStatus.BORROWING);
        assertThat(book.getAvailable()).isEqualTo(2);

        ArgumentCaptor<BorrowItem> itemCaptor = ArgumentCaptor.forClass(BorrowItem.class);
        verify(bookRepository).save(book);
        verify(borrowItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getBorrowId()).isEqualTo(99);
        assertThat(itemCaptor.getValue().getQuantity()).isEqualTo(2);
    }

    @Test
    void requestBorrow_shouldKeepAvailabilityUntilApproval() {
        Student student = new Student();
        student.setStudentId(7);
        Staff staff = new Staff();
        staff.setStaffId(3);
        Book book = new Book();
        book.setBookId(11);
        book.setBookName("Domain-Driven Design");
        book.setAvailable(5);

        BorrowItem item = new BorrowItem();
        item.setBookId(11);
        item.setQuantity(1);

        when(studentRepository.findById(7)).thenReturn(Optional.of(student));
        when(staffRepository.findById(3)).thenReturn(Optional.of(staff));
        when(bookRepository.findById(11)).thenReturn(Optional.of(book));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> {
            Borrow borrow = invocation.getArgument(0);
            borrow.setBorrowId(100);
            return borrow;
        });

        Borrow created = borrowService.requestBorrow(7, 3, List.of(item), LocalDate.now().plusDays(10));

        assertThat(created.getStatus()).isEqualTo(BorrowStatus.PENDING);
        assertThat(book.getAvailable()).isEqualTo(5);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void approveBorrow_shouldDecreaseAvailabilityAndFlipStatus() {
        Borrow borrow = new Borrow();
        borrow.setBorrowId(50);
        borrow.setStatus(BorrowStatus.PENDING);

        BorrowItem item = new BorrowItem();
        item.setBorrowId(50);
        item.setBookId(9);
        item.setQuantity(1);

        Book book = new Book();
        book.setBookId(9);
        book.setBookName("Refactoring");
        book.setAvailable(1);

        when(borrowRepository.findById(50)).thenReturn(Optional.of(borrow));
        when(borrowItemRepository.findByBorrowId(50)).thenReturn(List.of(item));
        when(bookRepository.findById(9)).thenReturn(Optional.of(book));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Borrow approved = borrowService.approveBorrow(50);

        assertThat(approved.getStatus()).isEqualTo(BorrowStatus.BORROWING);
        assertThat(book.getAvailable()).isZero();
        verify(bookRepository).save(book);
    }

    @Test
    void returnBorrow_shouldRestoreAvailabilityExactlyOnce() {
        Borrow borrow = new Borrow();
        borrow.setBorrowId(77);
        borrow.setStatus(BorrowStatus.BORROWING);

        BorrowItem item = new BorrowItem();
        item.setBorrowId(77);
        item.setBookId(4);
        item.setQuantity(2);

        Book book = new Book();
        book.setBookId(4);
        book.setBookName("Patterns of Enterprise Application Architecture");
        book.setAvailable(0);

        when(borrowRepository.findById(77)).thenReturn(Optional.of(borrow));
        when(borrowItemRepository.findByBorrowId(77)).thenReturn(List.of(item));
        when(bookRepository.findById(4)).thenReturn(Optional.of(book));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Borrow returned = borrowService.returnBorrow(77);

        assertThat(returned.getStatus()).isEqualTo(BorrowStatus.RETURNED);
        assertThat(returned.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(book.getAvailable()).isEqualTo(2);
        verify(bookRepository).save(book);
    }
}
