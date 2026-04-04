package com.library.feature.borrow;

import com.library.domain.model.Book;
import com.library.domain.model.BookHold;
import com.library.domain.model.Student;
import com.library.domain.repository.BookHoldRepository;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private BookHoldService bookHoldService;

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
                .isInstanceOf(IllegalArgumentException.class);

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
                .isInstanceOf(IllegalArgumentException.class);

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
                .isInstanceOf(IllegalArgumentException.class);
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
                .isInstanceOf(IllegalArgumentException.class);
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
    void countActiveByStudent_shouldUseRepositoryCountQuery() {
        when(bookHoldRepository.countByStudentStudentIdAndStatusIn(any(), any())).thenReturn(3L);

        long count = bookHoldService.countActiveByStudent(7);

        assertThat(count).isEqualTo(3);
        verify(bookHoldRepository, never()).findByStudentStudentIdAndStatusInOrderByHoldDateDesc(any(), any());
    }
}
