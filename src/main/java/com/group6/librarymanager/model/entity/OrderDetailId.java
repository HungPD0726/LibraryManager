package com.group6.librarymanager.model.entity;

import java.io.Serializable;
import java.util.Objects;

public class OrderDetailId implements Serializable {
    private Integer orderId;
    private Integer bookId;

    public OrderDetailId() {
    }

    public OrderDetailId(Integer orderId, Integer bookId) {
        this.orderId = orderId;
        this.bookId = bookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrderDetailId that = (OrderDetailId) o;
        return Objects.equals(orderId, that.orderId) && Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, bookId);
    }
}
