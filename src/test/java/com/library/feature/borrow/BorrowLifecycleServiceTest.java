package com.library.feature.borrow;

import com.library.domain.model.Borrow;
import com.library.domain.repository.BorrowRepository;
import com.library.shared.constant.BorrowStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowLifecycleServiceTest {

    @Mock
    private BorrowRepository borrowRepository;

    private BorrowLifecycleService borrowLifecycleService;
    private TrackingBorrowInventoryService borrowInventoryService;

    @BeforeEach
    void setUp() {
        borrowInventoryService = new TrackingBorrowInventoryService();
        borrowLifecycleService = new BorrowLifecycleService(borrowRepository, borrowInventoryService);
    }

    @Test
    void approveBorrow_shouldDecreaseAvailabilityAndFlipStatus() {
        Borrow borrow = new Borrow();
        borrow.setBorrowId(50);
        borrow.setStatus(BorrowStatus.PENDING);

        when(borrowRepository.findById(50)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Borrow approved = borrowLifecycleService.approveBorrow(50);

        assertThat(approved.getStatus()).isEqualTo(BorrowStatus.BORROWING);
        assertThat(borrowInventoryService.approvedBorrowId).isEqualTo(50);
    }

    @Test
    void returnBorrow_shouldRestoreAvailabilityExactlyOnce() {
        Borrow borrow = new Borrow();
        borrow.setBorrowId(77);
        borrow.setStatus(BorrowStatus.BORROWING);

        when(borrowRepository.findById(77)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Borrow returned = borrowLifecycleService.returnBorrow(77);

        assertThat(returned.getStatus()).isEqualTo(BorrowStatus.RETURNED);
        assertThat(returned.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(borrowInventoryService.restoredBorrowId).isEqualTo(77);
    }

    private static final class TrackingBorrowInventoryService extends BorrowInventoryService {

        private Integer approvedBorrowId;
        private Integer restoredBorrowId;

        private TrackingBorrowInventoryService() {
            super(null, null);
        }

        @Override
        public void decrementAvailabilityForBorrow(Integer borrowId) {
            this.approvedBorrowId = borrowId;
        }

        @Override
        public void restoreAvailabilityForBorrow(Integer borrowId) {
            this.restoredBorrowId = borrowId;
        }
    }
}
