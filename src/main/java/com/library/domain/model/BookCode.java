package com.library.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BookCode")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString
public class BookCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookCodeID")
    private Integer bookCodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookID", nullable = false)
    private Book book;

    @Column(name = "BookCode", nullable = false, length = 50, unique = true)
    private String code;
}
