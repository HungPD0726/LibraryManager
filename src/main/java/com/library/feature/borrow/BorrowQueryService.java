package com.library.feature.borrow;

import com.library.domain.model.Borrow;
import com.library.domain.model.BorrowItem;
import com.library.domain.repository.BorrowItemRepository;
import com.library.domain.repository.BorrowRepository;
import com.library.shared.constant.BorrowStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BorrowQueryService {

    private static final List<String> ACTIVE_STUDENT_STATUSES = List.of(
            BorrowStatus.PENDING,
            BorrowStatus.BORROWING,
            BorrowStatus.OVERDUE,
            BorrowStatus.RETURN_REQUESTED
    );

    private final BorrowRepository borrowRepository;
    private final BorrowItemRepository borrowItemRepository;

    @Transactional(readOnly = true)
    public Page<Borrow> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("borrowId").descending());
        return borrowRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Borrow> findByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("borrowId").descending());
        return borrowRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Borrow> findByStudentId(Integer studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("borrowId").descending());
        return borrowRepository.findByStudentStudentId(studentId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Borrow> findStudentHistory(Integer studentId) {
        return borrowRepository.findByStudentStudentIdOrderByBorrowIdDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<Borrow> findActiveByStudent(Integer studentId) {
        return borrowRepository.findByStudentStudentIdAndStatusInOrderByBorrowIdDesc(studentId, ACTIVE_STUDENT_STATUSES);
    }

    @Transactional(readOnly = true)
    public Optional<Borrow> findById(Integer id) {
        return borrowRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<BorrowItem> findItemsByBorrowId(Integer borrowId) {
        return borrowItemRepository.findByBorrowId(borrowId);
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return borrowRepository.countPending();
    }

    @Transactional(readOnly = true)
    public long countBorrowing() {
        return borrowRepository.countBorrowing();
    }

    @Transactional(readOnly = true)
    public long countOverdue() {
        return borrowRepository.countOverdue();
    }

    @Transactional(readOnly = true)
    public long countActiveByStudent(Integer studentId) {
        return borrowRepository.countByStudentStudentIdAndStatusIn(studentId, ACTIVE_STUDENT_STATUSES);
    }

    @Transactional(readOnly = true)
    public long countOverdueByStudent(Integer studentId) {
        return borrowRepository.countByStudentStudentIdAndStatus(studentId, BorrowStatus.OVERDUE);
    }
}
