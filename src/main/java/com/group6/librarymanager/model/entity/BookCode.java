package com.group6.librarymanager.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "BookCode")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookCodeID")
    private Integer bookCodeId;

    @ManyToOne
    @JoinColumn(name = "BookID", nullable = false)
    private Book book;

    @Column(name = "BookCode", nullable = false, unique = true, length = 50)
    private String bookCode;
}
