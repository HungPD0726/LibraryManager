package com.library.feature.fine;

import com.library.shared.constant.BorrowStatus;
import com.library.shared.constant.FineStatus;
import com.library.domain.model.Borrow;
import com.library.domain.model.Fine;
import com.library.domain.repository.BorrowRepository;
import com.library.domain.repository.FineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FineService {

    private static final BigDecimal DAILY_FINE_RATE = new BigDecimal("5000");

    private final FineRepository fineRepository;
    private final BorrowRepository borrowRepository;

    @Transactional(readOnly = true)
    public Page<Fine> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fineRepository.findAllPaged(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Fine> findByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fineRepository.findByStatusPaged(status, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Fine> findById(Integer id) {
        return fineRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Fine> findByBorrowId(Integer borrowId) {
        return fineRepository.findByBorrowBorrowId(borrowId);
    }

    @Transactional(readOnly = true)
    public List<Fine> findByStudentId(Integer studentId) {
        return fineRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<Fine> findUnpaidByStudentId(Integer studentId) {
        return fineRepository.findByStudentIdAndStatus(studentId, FineStatus.UNPAID);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumUnpaidByStudentId(Integer studentId) {
        return fineRepository.sumUnpaidByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public long countUnpaid() {
        return fineRepository.countByStatus(FineStatus.UNPAID);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumUnpaidAmount() {
        return fineRepository.sumUnpaidAmount();
    }

    @Transactional(readOnly = true)
    public BigDecimal sumPaidAmount() {
        return fineRepository.sumPaidAmount();
    }

    @Transactional
    public Fine createFine(Integer borrowId, BigDecimal amount, String reason) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mượn ID: " + borrowId));

        Fine fine = new Fine();
        fine.setBorrow(borrow);
        fine.setAmount(amount);
        fine.setReason(reason);
        fine.setCreatedDate(LocalDate.now());
        fine.setStatus(FineStatus.UNPAID);
        return fineRepository.save(fine);
    }

    @Transactional
    public Fine markPaid(Integer fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu phạt ID: " + fineId));

        if (FineStatus.PAID.equals(fine.getStatus())) {
            throw new IllegalArgumentException("Phiếu phạt đã được thanh toán.");
        }

        fine.setStatus(FineStatus.PAID);
        fine.setPaidDate(LocalDate.now());
        return fineRepository.save(fine);
    }

    @Transactional
    public int autoGenerateOverdueFines() {
        List<Borrow> overdueBorrows = borrowRepository.findByStatus(BorrowStatus.OVERDUE);
        int created = 0;

        for (Borrow borrow : overdueBorrows) {
            if (fineRepository.existsByBorrowBorrowId(borrow.getBorrowId())) {
                continue;
            }

            long daysLate = ChronoUnit.DAYS.between(borrow.getDueDate(), LocalDate.now());
            if (daysLate <= 0) {
                continue;
            }

            BigDecimal amount = DAILY_FINE_RATE.multiply(BigDecimal.valueOf(daysLate));
            String reason = "Trả sách trễ " + daysLate + " ngày (tự động)";

            Fine fine = new Fine();
            fine.setBorrow(borrow);
            fine.setAmount(amount);
            fine.setReason(reason);
            fine.setCreatedDate(LocalDate.now());
            fine.setStatus(FineStatus.UNPAID);
            fineRepository.save(fine);
            created++;
        }

        return created;
    }
}
