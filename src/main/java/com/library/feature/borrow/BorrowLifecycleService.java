package com.library.feature.borrow;

import com.library.domain.model.Borrow;
import com.library.domain.model.BorrowItem;
import com.library.domain.repository.BorrowItemRepository;
import com.library.domain.repository.BorrowRepository;
import com.library.shared.constant.BorrowStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowLifecycleService {

    private final BorrowRepository borrowRepository;
    private final BorrowItemRepository borrowItemRepository;
    private final BorrowInventoryService borrowInventoryService;
    private final BookHoldService bookHoldService;

    @Transactional
    public Borrow returnBorrow(Integer borrowId) {
        Borrow borrow = findBorrow(borrowId);
        if (!BorrowStatus.BORROWING.equals(borrow.getStatus()) && !BorrowStatus.OVERDUE.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chá»‰ cÃ³ thá»ƒ tráº£ sÃ¡ch cho Ä‘Æ¡n Ä‘ang mÆ°á»£n hoáº·c quÃ¡ háº¡n.");
        }

        borrow.setStatus(BorrowStatus.RETURNED);
        borrow.setReturnDate(LocalDate.now());
        borrowInventoryService.restoreAvailabilityForBorrow(borrowId);

        List<BorrowItem> returnedItems = borrowItemRepository.findByBorrowId(borrowId);
        Integer staffId = borrow.getStaff() != null ? borrow.getStaff().getStaffId() : null;
        bookHoldService.fulfillAvailableHoldsForReturnedItems(staffId, returnedItems);

        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow approveBorrow(Integer borrowId) {
        Borrow borrow = findBorrow(borrowId);
        if (!BorrowStatus.PENDING.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chá»‰ cÃ³ thá»ƒ duyá»‡t Ä‘Æ¡n Ä‘ang chá».");
        }

        borrowInventoryService.decrementAvailabilityForBorrow(borrowId);
        borrow.setStatus(BorrowStatus.BORROWING);
        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow rejectBorrow(Integer borrowId) {
        Borrow borrow = findBorrow(borrowId);
        if (!BorrowStatus.PENDING.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chá»‰ cÃ³ thá»ƒ tá»« chá»‘i Ä‘Æ¡n Ä‘ang chá».");
        }

        borrow.setStatus(BorrowStatus.REJECTED);
        return borrowRepository.save(borrow);
    }

    private Borrow findBorrow(Integer borrowId) {
        return borrowRepository.findById(borrowId)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n mÆ°á»£n ID: " + borrowId));
    }
}
