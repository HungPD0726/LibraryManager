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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowRequestService {

    private final BorrowRepository borrowRepository;
    private final BorrowItemRepository borrowItemRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final BorrowInventoryService borrowInventoryService;

    @Transactional
    public Borrow createBorrow(Integer studentId, Integer staffId, List<BorrowItem> items, LocalDate dueDate) {
        return createBorrowWithStatus(studentId, staffId, items, dueDate, BorrowStatus.BORROWING);
    }

    @Transactional
    public Borrow requestBorrow(Integer studentId, Integer staffId, List<BorrowItem> items, LocalDate dueDate) {
        return createBorrowWithStatus(studentId, staffId, items, dueDate, BorrowStatus.PENDING);
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

        items.forEach(borrowInventoryService::validateBorrowItem);
        if (BorrowStatus.BORROWING.equals(status)) {
            borrowInventoryService.decrementAvailabilityForItems(items, "không đủ số lượng.");
        }

        Borrow borrow = new Borrow();
        borrow.setStudent(student);
        borrow.setStaff(staff);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setDueDate(dueDate);
        borrow.setStatus(status);

        Borrow savedBorrow = borrowRepository.save(borrow);
        for (BorrowItem item : items) {
            item.setBorrowId(savedBorrow.getBorrowId());
            borrowItemRepository.save(item);
        }
        return savedBorrow;
    }
}
