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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final BorrowItemRepository borrowItemRepository;
    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;

    public Page<Borrow> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("borrowId").descending());
        return borrowRepository.findAll(pageable);
    }

    public Page<Borrow> findByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("borrowId").descending());
        return borrowRepository.findByStatus(status, pageable);
    }

    public Page<Borrow> findByStudentId(Integer studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("borrowId").descending());
        return borrowRepository.findByStudentStudentId(studentId, pageable);
    }

    public List<Borrow> findStudentHistory(Integer studentId) {
        return borrowRepository.findByStudentStudentIdOrderByBorrowIdDesc(studentId);
    }

    public List<Borrow> findActiveByStudent(Integer studentId) {
        return borrowRepository.findByStudentStudentIdAndStatusInOrderByBorrowIdDesc(
                studentId,
                List.of(BorrowStatus.PENDING, BorrowStatus.BORROWING, BorrowStatus.OVERDUE, BorrowStatus.RETURN_REQUESTED)
        );
    }

    public java.util.Optional<Borrow> findById(Integer id) {
        return borrowRepository.findById(id);
    }

    public List<BorrowItem> findItemsByBorrowId(Integer borrowId) {
        return borrowItemRepository.findByBorrowId(borrowId);
    }

    @Transactional
    public Borrow createBorrow(Integer studentId, Integer staffId, List<BorrowItem> items, LocalDate dueDate) {
        return createBorrowWithStatus(studentId, staffId, items, dueDate, BorrowStatus.BORROWING);
    }

    @Transactional
    public Borrow requestBorrow(Integer studentId, Integer staffId, List<BorrowItem> items, LocalDate dueDate) {
        return createBorrowWithStatus(studentId, staffId, items, dueDate, BorrowStatus.PENDING);
    }

    @Transactional
    public Borrow returnBorrow(Integer borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mượn ID: " + borrowId));

        if (!BorrowStatus.BORROWING.equals(borrow.getStatus()) && !BorrowStatus.OVERDUE.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể trả sách cho đơn đang mượn hoặc quá hạn.");
        }

        borrow.setStatus(BorrowStatus.RETURNED);
        borrow.setReturnDate(LocalDate.now());
        restoreBookAvailability(borrowId);
        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow approveBorrow(Integer borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mượn ID: " + borrowId));

        if (!BorrowStatus.PENDING.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể duyệt đơn đang chờ.");
        }

        decrementAvailabilityForBorrow(borrowId);
        borrow.setStatus(BorrowStatus.BORROWING);
        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow rejectBorrow(Integer borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mượn ID: " + borrowId));

        if (!BorrowStatus.PENDING.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể từ chối đơn đang chờ.");
        }

        borrow.setStatus(BorrowStatus.REJECTED);
        return borrowRepository.save(borrow);
    }

    public long countPending() {
        return borrowRepository.countPending();
    }

    public long countBorrowing() {
        return borrowRepository.countBorrowing();
    }

    public long countOverdue() {
        return borrowRepository.countOverdue();
    }

    public long countActiveByStudent(Integer studentId) {
        return findActiveByStudent(studentId).size();
    }

    public long countOverdueByStudent(Integer studentId) {
        return findStudentHistory(studentId).stream()
                .filter(borrow -> BorrowStatus.OVERDUE.equals(borrow.getStatus()))
                .count();
    }

    @Transactional
    protected Borrow createBorrowWithStatus(Integer studentId, Integer staffId, List<BorrowItem> items, LocalDate dueDate, String status) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên ID: " + studentId));
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên ID: " + staffId));

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sách mượn không được để trống.");
        }
        if (dueDate == null || dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Hạn trả không hợp lệ.");
        }

        Borrow borrow = new Borrow();
        borrow.setStudent(student);
        borrow.setStaff(staff);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setDueDate(dueDate);
        borrow.setStatus(status);

        Borrow savedBorrow = borrowRepository.save(borrow);

        for (BorrowItem item : items) {
            validateBorrowItem(item);
            Book book = bookRepository.findById(item.getBookId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách ID: " + item.getBookId()));
            if (book.getAvailable() < item.getQuantity()) {
                throw new IllegalArgumentException("Sách '" + book.getBookName() + "' không đủ số lượng.");
            }

            if (BorrowStatus.BORROWING.equals(status)) {
                book.setAvailable(book.getAvailable() - item.getQuantity());
                bookRepository.save(book);
            }

            item.setBorrowId(savedBorrow.getBorrowId());
            borrowItemRepository.save(item);
        }

        return savedBorrow;
    }

    private void validateBorrowItem(BorrowItem item) {
        if (item == null || item.getBookId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Thông tin sách mượn không hợp lệ.");
        }
    }

    private void decrementAvailabilityForBorrow(Integer borrowId) {
        List<BorrowItem> items = borrowItemRepository.findByBorrowId(borrowId);
        for (BorrowItem item : items) {
            Book book = bookRepository.findById(item.getBookId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách ID: " + item.getBookId()));
            if (book.getAvailable() < item.getQuantity()) {
                throw new IllegalArgumentException("Sách '" + book.getBookName() + "' không đủ số lượng để duyệt.");
            }
            book.setAvailable(book.getAvailable() - item.getQuantity());
            bookRepository.save(book);
        }
    }

    private void restoreBookAvailability(Integer borrowId) {
        List<BorrowItem> items = borrowItemRepository.findByBorrowId(borrowId);
        for (BorrowItem item : items) {
            Book book = bookRepository.findById(item.getBookId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách ID: " + item.getBookId()));
            book.setAvailable(book.getAvailable() + item.getQuantity());
            bookRepository.save(book);
        }
    }
}
