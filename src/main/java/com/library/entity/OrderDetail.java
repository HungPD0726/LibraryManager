package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "OrderDetail")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString
@IdClass(OrderDetailId.class)
public class OrderDetail {

    @Id
    @Column(name = "OrderID")
    private Integer orderId;

    @Id
    @Column(name = "BookID")
    private Integer bookId;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "UnitPrice", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderID", insertable = false, updatable = false)
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookID", insertable = false, updatable = false)
    private Book book;
}
