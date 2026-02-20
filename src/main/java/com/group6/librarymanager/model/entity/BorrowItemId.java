package com.group6.librarymanager.model.entity;

import java.io.Serializable;
import java.util.Objects;

public class BorrowItemId implements Serializable {
    private Integer borrowId;
    private Integer bookId;

    public BorrowItemId() {
    }

    public BorrowItemId(Integer borrowId, Integer bookId) {
        this.borrowId = borrowId;
        this.bookId = bookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BorrowItemId that = (BorrowItemId) o;
        return Objects.equals(borrowId, that.borrowId) && Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(borrowId, bookId);
    }
}
