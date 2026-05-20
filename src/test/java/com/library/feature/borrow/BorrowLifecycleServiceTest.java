package com.library.feature.borrow;

import com.library.domain.model.Borrow;
import com.library.domain.model.BorrowItem;
import com.library.domain.model.Staff;
import com.library.domain.repository.BorrowItemRepository;
import com.library.domain.repository.BorrowRepository;
import com.library.feature.notification.NotificationService;
import com.library.shared.constant.BorrowStatus;
import com.library.shared.realtime.AdminLiveUpdateService;
import com.library.shared.support.NotificationTextSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowLifecycleServiceTest {

    @Mock
    private BorrowRepository borrowRepository;
    @Mock
    private BorrowItemRepository borrowItemRepository;

    private BorrowLifecycleService borrowLifecycleService;
    private TrackingBorrowInventoryService borrowInventoryService;
    private TrackingBookHoldService bookHoldService;
    private RecordingNotificationService notificationService;

    @BeforeEach
    void setUp() {
        borrowInventoryService = new TrackingBorrowInventoryService();
        bookHoldService = new TrackingBookHoldService();
        notificationService = new RecordingNotificationService();
        borrowLifecycleService = new BorrowLifecycleService(
                borrowRepository,
                borrowItemRepository,
                borrowInventoryService,
                bookHoldService,
                notificationService
        );
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
    void returnBorrow_shouldRestoreAvailabilityAndFulfillQueuedHolds() {
        Staff staff = new Staff();
        staff.setStaffId(4);

        Borrow borrow = new Borrow();
        borrow.setBorrowId(77);
        borrow.setStatus(BorrowStatus.BORROWING);
        borrow.setStaff(staff);

        BorrowItem item = new BorrowItem();
        item.setBorrowId(77);
        item.setBookId(9);
        item.setQuantity(1);

        when(borrowRepository.findById(77)).thenReturn(Optional.of(borrow));
        when(borrowItemRepository.findByBorrowId(77)).thenReturn(List.of(item));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Borrow returned = borrowLifecycleService.returnBorrow(77);

        assertThat(returned.getStatus()).isEqualTo(BorrowStatus.RETURNED);
        assertThat(returned.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(borrowInventoryService.restoredBorrowId).isEqualTo(77);
        assertThat(bookHoldService.lastStaffId).isEqualTo(4);
        assertThat(bookHoldService.lastReturnedItems).containsExactly(item);
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

    private static final class TrackingBookHoldService extends BookHoldService {

        private Integer lastStaffId;
        private List<BorrowItem> lastReturnedItems = List.of();

        private TrackingBookHoldService() {
            super(
                    null,
                    null,
                    null,
                    new RecordingBorrowRequestService(),
                    new RecordingNotificationService(),
                    new RecordingAdminLiveUpdateService()
            );
        }

        @Override
        public List<Borrow> fulfillAvailableHoldsForReturnedItems(Integer staffId, List<BorrowItem> returnedItems) {
            this.lastStaffId = staffId;
            this.lastReturnedItems = returnedItems == null ? List.of() : List.copyOf(returnedItems);
            return List.of();
        }
    }

    private static final class RecordingBorrowRequestService extends BorrowRequestService {

        private RecordingBorrowRequestService() {
            super(null, null, null, null, null);
        }
    }

    private static final class RecordingNotificationService extends NotificationService {

        private RecordingNotificationService() {
            super(null, null, new NotificationTextSupport());
        }
    }

    private static final class RecordingAdminLiveUpdateService extends AdminLiveUpdateService {

        private RecordingAdminLiveUpdateService() {
            super(null, null);
        }
    }
}
