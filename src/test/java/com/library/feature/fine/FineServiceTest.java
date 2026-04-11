package com.library.feature.fine;

import com.library.domain.model.Fine;
import com.library.domain.repository.BorrowRepository;
import com.library.domain.repository.FineRepository;
import com.library.shared.constant.FineStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FineServiceTest {

    @Mock
    private FineRepository fineRepository;
    @Mock
    private BorrowRepository borrowRepository;

    @InjectMocks
    private FineService fineService;

    @Test
    void createFine_shouldThrowLocalizedMessageWhenBorrowIsMissing() {
        when(borrowRepository.findById(88)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fineService.createFine(88, new BigDecimal("5000"), "Trễ hạn"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Không tìm thấy đơn mượn ID: 88");
    }

    @Test
    void markPaid_shouldThrowLocalizedMessageWhenFineAlreadyPaid() {
        Fine fine = new Fine();
        fine.setFineId(9);
        fine.setStatus(FineStatus.PAID);

        when(fineRepository.findById(9)).thenReturn(Optional.of(fine));

        assertThatThrownBy(() -> fineService.markPaid(9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phiếu phạt đã được thanh toán.");
    }
}
