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
                "SÃ¡ch Ä‘Ã£ Ä‘Æ°á»£c tráº£",
                "ÃÆ¡n muá»£n #30 Ä‘Ã£ Ä‘Æ°á»£c xÃ¡c nháº­n tráº£ thÃ nh cÃ´ng.");

        assertThat(normalized.title()).isEqualTo("Sách đã được trả");
        assertThat(normalized.message()).isEqualTo("Đơn mượn #30 đã được xác nhận trả thành công.");
        assertThat(normalized.type()).isEqualTo(NotificationType.BORROW_RETURNED);
    }

    @Test
    void normalize_shouldRebuildDeliveredOrderNotificationFromCorruptedLegacyText() {
        NotificationTextSupport.NotificationText normalized = notificationTextSupport.normalize(
                NotificationType.ORDER_DELIVERED,
                "ÃÆ¡n mua Ä‘Ã£ giao",
                "ÃÆ¡n mua #18 Ä‘Ã£ Ä‘Æ°á»£c giao thÃ nh cÃ´ng.");

        assertThat(normalized.title()).isEqualTo("Đơn mua đã giao");
        assertThat(normalized.message()).isEqualTo("Đơn mua #18 đã được giao thành công.");
    }

    @Test
    void normalize_shouldUseCanonicalTitleForBorrowDueSoonNotifications() {
        NotificationTextSupport.NotificationText normalized = notificationTextSupport.normalize(
                NotificationType.BORROW_DUE_SOON,
                "Sắp đến hạn trả sách",
                "Đơn mượn #11 sẽ đến hạn vào ngày 12/04/2026. Vui lòng sắp xếp trả sách đúng hạn.");

        assertThat(normalized.title()).isEqualTo("Sắp đến hạn trả sách");
        assertThat(normalized.message()).contains("#11").contains("12/04/2026");
        assertThat(normalized.type()).isEqualTo(NotificationType.BORROW_DUE_SOON);
    }

    @Test
    void normalize_shouldUseCanonicalTitleForBorrowOverdueNotifications() {
        NotificationTextSupport.NotificationText normalized = notificationTextSupport.normalize(
                NotificationType.BORROW_OVERDUE,
                "Phiếu mượn đã quá hạn",
                "Đơn mượn #17 đã quá hạn 2 ngày kể từ 09/04/2026. Vui lòng trả sách sớm để tránh phát sinh xử lý thêm.");

        assertThat(normalized.title()).isEqualTo("Phiếu mượn đã quá hạn");
        assertThat(normalized.message()).contains("#17").contains("quá hạn 2 ngày");
        assertThat(normalized.type()).isEqualTo(NotificationType.BORROW_OVERDUE);
    }
}
