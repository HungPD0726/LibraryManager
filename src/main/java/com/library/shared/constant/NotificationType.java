package com.library.shared.constant;

public final class NotificationType {

    public static final String BORROW_APPROVED = "BORROW_APPROVED";
    public static final String BORROW_REJECTED = "BORROW_REJECTED";
    public static final String BORROW_RETURNED = "BORROW_RETURNED";
    public static final String BORROW_DUE_SOON = "BORROW_DUE_SOON";
    public static final String BORROW_OVERDUE = "BORROW_OVERDUE";
    public static final String FINE_CREATED = "FINE_CREATED";
    public static final String FINE_PAID = "FINE_PAID";
    public static final String HOLD_FULFILLED = "HOLD_FULFILLED";
    public static final String ORDER_DELIVERED = "ORDER_DELIVERED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String GENERAL = "GENERAL";

    private NotificationType() {
    }
}
