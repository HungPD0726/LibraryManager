package com.group6.librarymanager.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "OrderDetail")
@IdClass(OrderDetailId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne
    @JoinColumn(name = "OrderID", insertable = false, updatable = false)
    @JsonIgnore
    private Orders order;

    @ManyToOne
    @JoinColumn(name = "BookID", insertable = false, updatable = false)
    private Book book;
}
