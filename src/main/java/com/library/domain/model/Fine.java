package com.library.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Fine")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FineID")
    private Integer fineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BorrowID", nullable = false)
    private Borrow borrow;

    @Column(name = "Amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "Reason", length = 200)
    private String reason;

    @Column(name = "CreatedDate")
    private LocalDate createdDate;

    @Column(name = "PaidDate")
    private LocalDate paidDate;

    @Column(name = "Status", length = 20)
    private String status;
}
