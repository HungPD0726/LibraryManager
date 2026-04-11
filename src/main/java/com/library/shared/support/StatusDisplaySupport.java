package com.library.shared.support;

import com.library.shared.constant.BorrowStatus;
import com.library.shared.constant.FineStatus;
import com.library.shared.constant.OrderStatus;
import org.springframework.stereotype.Component;

@Component("statusDisplay")
public class StatusDisplaySupport {

    public String borrowLabel(String status) {
        String normalized = normalize(status);
        if (normalized == null || normalized.isEmpty()) {
            return "Không xác định";
        }
        return switch (normalized) {
            case BorrowStatus.PENDING -> "Chờ duyệt";
            case BorrowStatus.BORROWING -> "Đang mượn";
            case BorrowStatus.RETURNED -> "Đã trả";
            case BorrowStatus.OVERDUE -> "Quá hạn";
            case BorrowStatus.REJECTED -> "Từ chối";
            case BorrowStatus.RETURN_REQUESTED -> "Yêu cầu trả";
            default -> status.trim();
        };
    }

    public String borrowTone(String status) {
        String normalized = normalize(status);
        if (normalized == null || normalized.isEmpty()) {
            return "secondary";
        }
        return switch (normalized) {
            case BorrowStatus.RETURNED -> "success";
            case BorrowStatus.BORROWING -> "info";
            case BorrowStatus.PENDING, BorrowStatus.RETURN_REQUESTED -> "warning";
            case BorrowStatus.OVERDUE, BorrowStatus.REJECTED -> "danger";
            default -> "secondary";
        };
    }

    public String holdLabel(String status) {
        String normalized = normalize(status);
        if (normalized == null || normalized.isEmpty()) {
            return "Không xác định";
        }
        return switch (normalized) {
            case "Waiting" -> "Đang chờ";
            case "Notified" -> "Đã báo có";
            case "Cancelled" -> "Đã hủy";
            default -> status.trim();
        };
    }

    public String holdTone(String status) {
        String normalized = normalize(status);
        if (normalized == null || normalized.isEmpty()) {
            return "secondary";
        }
        return switch (normalized) {
            case "Notified" -> "success";
            case "Waiting" -> "warning";
            case "Cancelled" -> "secondary";
            default -> "secondary";
        };
    }

    public String orderLabel(String status) {
        String normalized = OrderStatus.normalize(status);
        if (normalized == null || normalized.isEmpty()) {
            return "Không xác định";
        }
        return switch (normalized) {
            case OrderStatus.READY -> "Sẵn sàng";
            case OrderStatus.WAITING -> "Hàng chờ";
            case OrderStatus.DELIVERED -> "Đã giao";
            case OrderStatus.CANCELLED -> "Đã hủy";
            case OrderStatus.LEGACY_PENDING -> "Đang chờ xử lý";
            case OrderStatus.LEGACY_APPROVED -> "Đã duyệt";
            case OrderStatus.LEGACY_REJECTED -> "Đã từ chối";
            default -> normalized;
        };
    }

    public String orderTone(String status) {
        String normalized = OrderStatus.normalize(status);
        if (normalized == null || normalized.isEmpty()) {
            return "secondary";
        }
        return switch (normalized) {
            case OrderStatus.DELIVERED -> "success";
            case OrderStatus.READY, OrderStatus.LEGACY_APPROVED -> "info";
            case OrderStatus.WAITING, OrderStatus.LEGACY_PENDING -> "warning";
            case OrderStatus.CANCELLED, OrderStatus.LEGACY_REJECTED -> "danger";
            default -> "secondary";
        };
    }

    public String fineLabel(String status) {
        String normalized = normalize(status);
        if (normalized == null || normalized.isEmpty()) {
            return "Không xác định";
        }
        return switch (normalized) {
            case FineStatus.PAID -> "Đã thanh toán";
            case FineStatus.UNPAID -> "Chưa thanh toán";
            default -> status.trim();
        };
    }

    public String fineTone(String status) {
        String normalized = normalize(status);
        if (normalized == null || normalized.isEmpty()) {
            return "secondary";
        }
        return switch (normalized) {
            case FineStatus.PAID -> "success";
            case FineStatus.UNPAID -> "danger";
            default -> "secondary";
        };
    }

    public boolean isLegacyPendingOrder(String status) {
        return OrderStatus.LEGACY_PENDING.equals(OrderStatus.normalize(status));
    }

    public boolean canCompleteOrder(String status) {
        return OrderStatus.canBeDelivered(status);
    }

    public boolean isClosedOrder(String status) {
        return OrderStatus.isClosed(status);
    }

    private String normalize(String status) {
        return status == null ? null : status.trim();
    }
}
