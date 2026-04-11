package com.library.shared.constant;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

public final class OrderStatus {

    public static final String READY = "Sẵn sàng";
    public static final String WAITING = "Hàng chờ";
    public static final String DELIVERED = "Đã giao";
    public static final String CANCELLED = "Đã hủy";
    public static final String LEGACY_PENDING = "Pending";
    public static final String LEGACY_APPROVED = "Approved";
    public static final String LEGACY_REJECTED = "Rejected";

    private static final String READY_MOJIBAKE = "SÃ¡ÂºÂµn sÃƒÂ ng";
    private static final String WAITING_MOJIBAKE = "HÃƒÂ ng chÃ¡Â»Â";
    private static final String DELIVERED_MOJIBAKE = "Ã„ÂÃƒÂ£ giao";
    private static final String CANCELLED_MOJIBAKE = "Ã„ÂÃƒÂ£ hÃ¡Â»Â§y";

    private static final Set<String> READY_VALUES = buildValues(READY, READY_MOJIBAKE);
    private static final Set<String> WAITING_VALUES = buildValues(WAITING, WAITING_MOJIBAKE);
    private static final Set<String> DELIVERED_VALUES = buildValues(DELIVERED, DELIVERED_MOJIBAKE);
    private static final Set<String> CANCELLED_VALUES = buildValues(CANCELLED, CANCELLED_MOJIBAKE);
    private static final Set<String> LEGACY_PENDING_VALUES = buildValues(LEGACY_PENDING);
    private static final Set<String> LEGACY_APPROVED_VALUES = buildValues(LEGACY_APPROVED);
    private static final Set<String> LEGACY_REJECTED_VALUES = buildValues(LEGACY_REJECTED);

    private OrderStatus() {
    }

    public static String normalize(String status) {
        if (status == null) {
            return null;
        }

        String trimmed = status.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        String repaired = repairMojibake(trimmed);
        if (matches(trimmed, repaired, READY_VALUES)) {
            return READY;
        }
        if (matches(trimmed, repaired, WAITING_VALUES)) {
            return WAITING;
        }
        if (matches(trimmed, repaired, DELIVERED_VALUES)) {
            return DELIVERED;
        }
        if (matches(trimmed, repaired, CANCELLED_VALUES)) {
            return CANCELLED;
        }
        if (matches(trimmed, repaired, LEGACY_PENDING_VALUES)) {
            return LEGACY_PENDING;
        }
        if (matches(trimmed, repaired, LEGACY_APPROVED_VALUES)) {
            return LEGACY_APPROVED;
        }
        if (matches(trimmed, repaired, LEGACY_REJECTED_VALUES)) {
            return LEGACY_REJECTED;
        }
        return repaired;
    }

    public static boolean canBeDelivered(String status) {
        String normalized = normalize(status);
        return READY.equals(normalized)
                || WAITING.equals(normalized)
                || LEGACY_PENDING.equals(normalized);
    }

    public static boolean isLegacyResolved(String status) {
        String normalized = normalize(status);
        return LEGACY_APPROVED.equals(normalized) || LEGACY_REJECTED.equals(normalized);
    }

    public static boolean isClosed(String status) {
        String normalized = normalize(status);
        return DELIVERED.equals(normalized)
                || CANCELLED.equals(normalized)
                || isLegacyResolved(normalized);
    }

    public static Set<String> storedValuesFor(String status) {
        String normalized = normalize(status);
        if (READY.equals(normalized)) {
            return READY_VALUES;
        }
        if (WAITING.equals(normalized)) {
            return WAITING_VALUES;
        }
        if (DELIVERED.equals(normalized)) {
            return DELIVERED_VALUES;
        }
        if (CANCELLED.equals(normalized)) {
            return CANCELLED_VALUES;
        }
        if (LEGACY_PENDING.equals(normalized)) {
            return LEGACY_PENDING_VALUES;
        }
        if (LEGACY_APPROVED.equals(normalized)) {
            return LEGACY_APPROVED_VALUES;
        }
        if (LEGACY_REJECTED.equals(normalized)) {
            return LEGACY_REJECTED_VALUES;
        }
        return normalized == null || normalized.isBlank() ? Set.of() : Set.of(normalized);
    }

    private static Set<String> buildValues(String canonical, String... additionalVariants) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        addValue(values, canonical);
        for (String additionalVariant : additionalVariants) {
            addValue(values, additionalVariant);
            addValue(values, repairMojibake(additionalVariant));
        }
        return Set.copyOf(values);
    }

    private static void addValue(Set<String> values, String value) {
        if (value != null && !value.isBlank()) {
            values.add(value);
        }
    }

    private static boolean matches(String original, String repaired, Set<String> candidates) {
        return candidates.contains(original) || candidates.contains(repaired);
    }

    private static String repairMojibake(String value) {
        String repaired = value;
        for (int attempts = 0; attempts < 3; attempts++) {
            if (!looksLikeMojibake(repaired)) {
                break;
            }
            repaired = new String(repaired.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8).trim();
        }
        return repaired;
    }

    private static boolean looksLikeMojibake(String value) {
        return value.contains("Ã")
                || value.contains("Â")
                || value.contains("Ä")
                || value.contains("ã")
                || value.contains("â")
                || value.contains("ä");
    }
}
