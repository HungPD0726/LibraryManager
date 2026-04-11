package com.library.shared.support;

import com.library.shared.constant.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusDisplaySupportTest {

    private final StatusDisplaySupport statusDisplaySupport = new StatusDisplaySupport();

    @Test
    void shouldReturnLocalizedLabelsForAllFrontendStatuses() {
        assertThat(statusDisplaySupport.borrowLabel("Pending")).isEqualTo("Chờ duyệt");
        assertThat(statusDisplaySupport.holdLabel("Waiting")).isEqualTo("Đang chờ");
        assertThat(statusDisplaySupport.orderLabel("Pending")).isEqualTo("Đang chờ xử lý");
        assertThat(statusDisplaySupport.orderLabel("SÃ¡ÂºÂµn sÃƒÂ ng")).isEqualTo("Sẵn sàng");
        assertThat(statusDisplaySupport.fineLabel("Paid")).isEqualTo("Đã thanh toán");
    }

    @Test
    void shouldReturnExpectedTonesAndOrderFlags() {
        assertThat(statusDisplaySupport.borrowTone("Overdue")).isEqualTo("danger");
        assertThat(statusDisplaySupport.holdTone("Notified")).isEqualTo("success");
        assertThat(statusDisplaySupport.orderTone(OrderStatus.WAITING)).isEqualTo("warning");
        assertThat(statusDisplaySupport.fineTone("Unpaid")).isEqualTo("danger");
        assertThat(statusDisplaySupport.isLegacyPendingOrder("Pending")).isTrue();
        assertThat(statusDisplaySupport.canCompleteOrder(OrderStatus.READY)).isTrue();
        assertThat(statusDisplaySupport.isClosedOrder(OrderStatus.DELIVERED)).isTrue();
    }
}
