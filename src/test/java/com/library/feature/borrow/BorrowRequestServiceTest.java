package com.library.feature.borrow;

import com.library.domain.model.Borrow;
import com.library.domain.model.BorrowItem;
import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.BorrowItemRepository;
import com.library.domain.repository.BorrowRepository;
import com.library.domain.repository.StaffRepository;
import com.library.domain.repository.StudentRepository;
import com.library.shared.constant.BorrowStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowRequestServiceTest {

    @Mock
    private BorrowRepository borrowRepository;
    @Mock
    private BorrowItemRepository borrowItemRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StaffRepository staffRepository;

    private BorrowRequestService borrowRequestService;
    private TrackingBorrowInventoryService borrowInventoryService;

    @BeforeEach
    void setUp() {
        borrowInventoryService = new TrackingBorrowInventoryService();
        borrowRequestService = new BorrowRequestService(
                borrowRepository,
                borrowItemRepository,
                studentRepository,
                staffRepository,
                borrowInventoryService
        );
    }

    @Test
    void createBorrow_shouldDecreaseAvailabilityImmediately() {
        Student student = new Student();
        student.setStudentId(7);
        Staff staff = new Staff();
        staff.setStaffId(3);
        BorrowItem item = new BorrowItem();
        item.setBookId(10);
        item.setQuantity(2);

        when(studentRepository.findById(7)).thenReturn(Optional.of(student));
        when(staffRepository.findById(3)).thenReturn(Optional.of(staff));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> {
            Borrow borrow = invocation.getArgument(0);
            borrow.setBorrowId(99);
            return borrow;
        });

        Borrow created = borrowRequestService.createBorrow(7, 3, List.of(item), LocalDate.now().plusDays(7));

        assertThat(created.getStatus()).isEqualTo(BorrowStatus.BORROWING);
        assertThat(borrowInventoryService.validateCalls).isEqualTo(1);
        assertThat(borrowInventoryService.decrementedItems).containsExactly(item);
        assertThat(borrowInventoryService.lastInsufficientMessage).isNotBlank();

        ArgumentCaptor<BorrowItem> itemCaptor = ArgumentCaptor.forClass(BorrowItem.class);
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
        BorrowItem item = new BorrowItem();
        item.setBookId(11);
        item.setQuantity(1);

        when(studentRepository.findById(7)).thenReturn(Optional.of(student));
        when(staffRepository.findById(3)).thenReturn(Optional.of(staff));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> {
            Borrow borrow = invocation.getArgument(0);
            borrow.setBorrowId(100);
            return borrow;
        });

        Borrow created = borrowRequestService.requestBorrow(7, 3, List.of(item), LocalDate.now().plusDays(10));

        assertThat(created.getStatus()).isEqualTo(BorrowStatus.PENDING);
        assertThat(borrowInventoryService.validateCalls).isEqualTo(1);
        assertThat(borrowInventoryService.decrementedItems).isNull();
    }

    private static final class TrackingBorrowInventoryService extends BorrowInventoryService {

        private int validateCalls;
        private List<BorrowItem> decrementedItems;
        private String lastInsufficientMessage;

        private TrackingBorrowInventoryService() {
            super(null, null);
        }

        @Override
        public void decrementAvailabilityForItems(List<BorrowItem> items, String insufficientMessage) {
            this.decrementedItems = items;
            this.lastInsufficientMessage = insufficientMessage;
        }

        @Override
        public void validateBorrowItem(BorrowItem item) {
            validateCalls++;
        }
    }
}
