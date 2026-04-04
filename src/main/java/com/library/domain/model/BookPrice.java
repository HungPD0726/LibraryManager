package com.library.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "BookPrice")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString
@IdClass(BookPriceId.class)
public class BookPrice {

    @Id
    @Column(name = "BookID")
    private Integer bookId;

    @Id
    @Column(name = "PriceID")
    private Integer priceId;

    @Id
    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookID", insertable = false, updatable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PriceID", insertable = false, updatable = false)
    private Price price;
}
