package com.library.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "BookHold")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"student", "book"})
public class BookHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HoldID")
    private Integer holdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentID", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookID", nullable = false)
    private Book book;

    @Column(name = "HoldDate", nullable = false)
    private LocalDateTime holdDate;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "NotifiedDate")
    private LocalDateTime notifiedDate;

    @Column(name = "ExpireDate")
    private LocalDateTime expireDate;

    @Column(name = "Note", length = 200)
    private String note;

    @PrePersist
    void applyDefaults() {
        if (holdDate == null) {
            holdDate = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "Waiting";
        }
    }
}
