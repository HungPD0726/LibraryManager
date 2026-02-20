package com.group6.librarymanager.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Borrow")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BorrowID")
    private Integer borrowId;

    @ManyToOne
    @JoinColumn(name = "StudentID", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "StaffID", nullable = false)
    private Staff staff;

    @Column(name = "BorrowDate", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "DueDate", nullable = false)
    private LocalDate dueDate;

    @Column(name = "Status", nullable = false, length = 20)
    private String status; // Borrowing, Returned, Overdue

    @OneToMany(mappedBy = "borrow", cascade = CascadeType.ALL)
    private List<BorrowItem> borrowItems;
}
