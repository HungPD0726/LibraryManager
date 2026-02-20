package com.group6.librarymanager.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "BorrowItem")
@IdClass(BorrowItemId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowItem {

    @Id
    @Column(name = "BorrowID")
    private Integer borrowId;

    @Id
    @Column(name = "BookID")
    private Integer bookId;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "BorrowID", insertable = false, updatable = false)
    @JsonIgnore
    private Borrow borrow;

    @ManyToOne
    @JoinColumn(name = "BookID", insertable = false, updatable = false)
    private Book book;
}
