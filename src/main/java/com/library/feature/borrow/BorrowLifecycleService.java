package com.library.feature.borrow;

import com.library.domain.model.Borrow;
import com.library.domain.repository.BorrowRepository;
import com.library.shared.constant.BorrowStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BorrowLifecycleService {

    private final BorrowRepository borrowRepository;
    private final BorrowInventoryService borrowInventoryService;

    @Transactional
    public Borrow returnBorrow(Integer borrowId) {
        Borrow borrow = findBorrow(borrowId);
        if (!BorrowStatus.BORROWING.equals(borrow.getStatus()) && !BorrowStatus.OVERDUE.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể trả sách cho đơn đang mượn hoặc quá hạn.");
        }

        borrow.setStatus(BorrowStatus.RETURNED);
        borrow.setReturnDate(LocalDate.now());
        borrowInventoryService.restoreAvailabilityForBorrow(borrowId);
        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow approveBorrow(Integer borrowId) {
        Borrow borrow = findBorrow(borrowId);
        if (!BorrowStatus.PENDING.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể duyệt đơn đang chờ.");
        }

        borrowInventoryService.decrementAvailabilityForBorrow(borrowId);
        borrow.setStatus(BorrowStatus.BORROWING);
        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow rejectBorrow(Integer borrowId) {
        Borrow borrow = findBorrow(borrowId);
        if (!BorrowStatus.PENDING.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể từ chối đơn đang chờ.");
        }

        borrow.setStatus(BorrowStatus.REJECTED);
        return borrowRepository.save(borrow);
    }

    private Borrow findBorrow(Integer borrowId) {
        return borrowRepository.findById(borrowId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mượn ID: " + borrowId));
    }
}
