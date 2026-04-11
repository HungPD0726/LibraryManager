package com.library.shared.support;

import com.library.domain.model.Notification;
import com.library.shared.constant.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NotificationTextSupport {

    private static final Pattern REFERENCE_ID_PATTERN = Pattern.compile("#(\\d+)");

    public Notification normalize(Notification source) {
        if (source == null) {
            return null;
        }

        NotificationText normalized = normalize(source.getType(), source.getTitle(), source.getMessage());

        Notification copy = new Notification();
        copy.setNotificationId(source.getNotificationId());
        copy.setStudent(source.getStudent());
        copy.setType(normalized.type());
        copy.setTitle(normalized.title());
        copy.setMessage(normalized.message());
        copy.setIsRead(source.getIsRead());
        copy.setCreatedDate(source.getCreatedDate());
        return copy;
    }

    public NotificationText normalize(String type, String title, String message) {
        String normalizedType = normalizeType(type);
        String repairedTitle = repairMojibake(title);
        String repairedMessage = repairMojibake(message);

        if (!isSuspectedCorrupted(title) && !isSuspectedCorrupted(message)) {
            return new NotificationText(repairedTitle, repairedMessage, normalizedType);
        }

        Integer referenceId = extractReferenceId(repairedMessage, repairedTitle);
        return switch (normalizedType) {
            case NotificationType.BORROW_APPROVED -> new NotificationText(
                    "Yêu cầu mượn được duyệt",
                    referenceId == null
                            ? repairedMessage
                            : "Đơn mượn #" + referenceId + " đã được duyệt. Vui lòng đến thư viện để nhận sách.",
                    normalizedType
            );
            case NotificationType.BORROW_REJECTED -> new NotificationText(
                    "Yêu cầu mượn bị từ chối",
                    referenceId == null
                            ? repairedMessage
                            : "Đơn mượn #" + referenceId + " đã bị từ chối.",
                    normalizedType
            );
            case NotificationType.BORROW_RETURNED -> new NotificationText(
                    "Sách đã được trả",
                    referenceId == null
                            ? repairedMessage
                            : "Đơn mượn #" + referenceId + " đã được xác nhận trả thành công.",
                    normalizedType
            );
            case NotificationType.BORROW_DUE_SOON -> new NotificationText(
                    "Sắp đến hạn trả sách",
                    repairedMessage,
                    normalizedType
            );
            case NotificationType.BORROW_OVERDUE -> new NotificationText(
                    "Phiếu mượn đã quá hạn",
                    repairedMessage,
                    normalizedType
            );
            case NotificationType.ORDER_DELIVERED -> new NotificationText(
                    "Đơn mua đã giao",
                    referenceId == null
                            ? repairedMessage
                            : "Đơn mua #" + referenceId + " đã được giao thành công.",
                    normalizedType
            );
            case NotificationType.ORDER_CANCELLED -> new NotificationText(
                    "Đơn mua bị hủy",
                    referenceId == null
                            ? repairedMessage
                            : "Đơn mua #" + referenceId + " đã bị hủy.",
                    normalizedType
            );
            case NotificationType.FINE_PAID -> new NotificationText(
                    "Phạt đã thanh toán",
                    referenceId == null
                            ? repairedMessage
                            : "Phiếu phạt #" + referenceId + " đã được xác nhận thanh toán.",
                    normalizedType
            );
            case NotificationType.FINE_CREATED -> new NotificationText(
                    "Phạt mới được tạo",
                    repairedMessage,
                    normalizedType
            );
            default -> new NotificationText(repairedTitle, repairedMessage, normalizedType);
        };
    }

    private String normalizeType(String type) {
        if (!StringUtils.hasText(type)) {
            return NotificationType.GENERAL;
        }
        return type.trim();
    }

    private Integer extractReferenceId(String... values) {
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            Matcher matcher = REFERENCE_ID_PATTERN.matcher(value);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return null;
    }

    private boolean isSuspectedCorrupted(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        String trimmed = value.trim();
        return trimmed.contains("?") || looksLikeMojibake(trimmed);
    }

    private String repairMojibake(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        String repaired = value.trim();
        for (int attempt = 0; attempt < 3; attempt++) {
            if (!looksLikeMojibake(repaired)) {
                break;
            }
            repaired = new String(repaired.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8).trim();
        }
        return repaired;
    }

    private boolean looksLikeMojibake(String value) {
        return value.contains("Ãƒ")
                || value.contains("Ã„")
                || value.contains("Ã†")
                || value.contains("Ã");
    }

    public record NotificationText(String title, String message, String type) {
    }
}
