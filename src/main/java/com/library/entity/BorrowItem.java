package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BorrowItem")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString
@IdClass(BorrowItemId.class)
public class BorrowItem {

    @Id
    @Column(name = "BorrowID")
    private Integer borrowId;

    @Id
    @Column(name = "BookID")
    private Integer bookId;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BorrowID", insertable = false, updatable = false)
    private Borrow borrow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookID", insertable = false, updatable = false)
    private Book book;
}
