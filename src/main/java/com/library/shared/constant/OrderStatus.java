package com.library.shared.constant;

import java.util.Set;

public final class OrderStatus {

    public static final String READY = "SÃ¡ÂºÂµn sÃƒÂ ng";
    public static final String WAITING = "HÃƒÂ ng chÃ¡Â»Â";
    public static final String DELIVERED = "Ã„ÂÃƒÂ£ giao";
    public static final String CANCELLED = "Ã„ÂÃƒÂ£ hÃ¡Â»Â§y";
    public static final String LEGACY_PENDING = "Pending";
    public static final String LEGACY_APPROVED = "Approved";
    public static final String LEGACY_REJECTED = "Rejected";

    private static final Set<String> ACTIVE_FOR_DELIVERY = Set.of(
            READY,
            WAITING,
            LEGACY_PENDING
    );

    private OrderStatus() {
    }

    public static boolean canBeDelivered(String status) {
        return ACTIVE_FOR_DELIVERY.contains(status);
    }

    public static boolean isLegacyResolved(String status) {
        return LEGACY_APPROVED.equals(status) || LEGACY_REJECTED.equals(status);
    }

    public static boolean isClosed(String status) {
        return DELIVERED.equals(status) || CANCELLED.equals(status) || isLegacyResolved(status);
    }
}
