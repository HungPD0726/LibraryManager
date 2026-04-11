package com.library.shared.support;

import com.library.shared.constant.NotificationType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTextSupportTest {

    private final NotificationTextSupport notificationTextSupport = new NotificationTextSupport();

    @Test
    void normalize_shouldRebuildBorrowReturnNotificationFromCorruptedLegacyText() {
        NotificationTextSupport.NotificationText normalized = notificationTextSupport.normalize(
                NotificationType.BORROW_RETURNED,
                "Sách đã được trả",
                "Ðơn muợn #30 đã được xác nhận trả thành công.");

        assertThat(normalized.title()).isEqualTo("Sách đã được trả");
        assertThat(normalized.message()).isEqualTo("Đơn mượn #30 đã được xác nhận trả thành công.");
        assertThat(normalized.type()).isEqualTo(NotificationType.BORROW_RETURNED);
    }

    @Test
    void normalize_shouldRebuildDeliveredOrderNotificationFromCorruptedLegacyText() {
        NotificationTextSupport.NotificationText normalized = notificationTextSupport.normalize(
                NotificationType.ORDER_DELIVERED,
                "Ðơn mua đã giao",
                "Ðơn mua #18 đã được giao thành công.");

        assertThat(normalized.title()).isEqualTo("Đơn mua đã giao");
        assertThat(normalized.message()).isEqualTo("Đơn mua #18 đã được giao thành công.");
    }

    @Test
    void normalize_shouldPreserveFineCreatedBodyWhileRepairingKnownTitle() {
        NotificationTextSupport.NotificationText normalized = notificationTextSupport.normalize(
                NotificationType.FINE_CREATED,
                "Phạt mới được tạo",
                "Bạn có phiếu phạt mơi: 25000 VND. Lý do: Trễ hạn trả");

        assertThat(normalized.title()).isEqualTo("Phạt mới được tạo");
        assertThat(normalized.message()).isEqualTo("Bạn có phiếu phạt mơi: 25000 VND. Lý do: Trễ hạn trả");
    }
}
