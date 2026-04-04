package com.library.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "Borrow")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"student", "staff"})
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BorrowID")
    private Integer borrowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentID", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StaffID")
    private Staff staff;

    @Column(name = "BorrowDate", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "DueDate", nullable = false)
    private LocalDate dueDate;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "ReturnDate")
    private LocalDate returnDate;
}
