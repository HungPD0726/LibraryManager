package com.library.feature.borrow;

import com.library.domain.model.Borrow;
import com.library.domain.model.BorrowItem;
import com.library.domain.repository.BorrowItemRepository;
import com.library.domain.repository.BorrowRepository;
import com.library.feature.notification.NotificationService;
import com.library.shared.constant.BorrowStatus;
import com.library.shared.constant.NotificationType;
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
    private final NotificationService notificationService;

    @Transactional
    public Borrow returnBorrow(Integer borrowId) {
        Borrow borrow = findBorrow(borrowId);
        if (!BorrowStatus.BORROWING.equals(borrow.getStatus()) && !BorrowStatus.OVERDUE.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể trả sách cho đơn đang mượn hoặc quá hạn.");
        }

        borrow.setStatus(BorrowStatus.RETURNED);
        borrow.setReturnDate(LocalDate.now());
        borrowInventoryService.restoreAvailabilityForBorrow(borrowId);

        List<BorrowItem> returnedItems = borrowItemRepository.findByBorrowId(borrowId);
        Integer staffId = borrow.getStaff() != null ? borrow.getStaff().getStaffId() : null;
        bookHoldService.fulfillAvailableHoldsForReturnedItems(staffId, returnedItems);

        Borrow saved = borrowRepository.save(borrow);

        if (saved.getStudent() != null) {
            notificationService.create(
                    saved.getStudent().getStudentId(),
                    "Sách đã được trả",
                    "Đơn mượn #" + borrowId + " đã được xác nhận trả thành công.",
                    NotificationType.BORROW_RETURNED
            );
        }
        return saved;
    }

    @Transactional
    public Borrow approveBorrow(Integer borrowId) {
        Borrow borrow = findBorrow(borrowId);
        if (!BorrowStatus.PENDING.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể duyệt đơn đang chờ.");
        }

        borrowInventoryService.decrementAvailabilityForBorrow(borrowId);
        borrow.setStatus(BorrowStatus.BORROWING);
        Borrow saved = borrowRepository.save(borrow);

        if (saved.getStudent() != null) {
            notificationService.create(
                    saved.getStudent().getStudentId(),
                    "Yêu cầu mượn được duyệt",
                    "Đơn mượn #" + borrowId + " đã được duyệt. Vui lòng đến thư viện để nhận sách.",
                    NotificationType.BORROW_APPROVED
            );
        }
        return saved;
    }

    @Transactional
    public Borrow rejectBorrow(Integer borrowId) {
        Borrow borrow = findBorrow(borrowId);
        if (!BorrowStatus.PENDING.equals(borrow.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể từ chối đơn đang chờ.");
        }

        borrow.setStatus(BorrowStatus.REJECTED);
        Borrow saved = borrowRepository.save(borrow);

        if (saved.getStudent() != null) {
            notificationService.create(
                    saved.getStudent().getStudentId(),
                    "Yêu cầu mượn bị từ chối",
                    "Đơn mượn #" + borrowId + " đã bị từ chối.",
                    NotificationType.BORROW_REJECTED
            );
        }
        return saved;
    }

    private Borrow findBorrow(Integer borrowId) {
        return borrowRepository.findById(borrowId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mượn ID: " + borrowId));
    }
}
