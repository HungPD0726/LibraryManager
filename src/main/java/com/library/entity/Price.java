package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Price")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PriceID")
    private Integer priceId;

    @Column(name = "Amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "Currency", length = 10)
    private String currency = "VND";

    @Column(name = "Note", length = 200)
    private String note;
}
