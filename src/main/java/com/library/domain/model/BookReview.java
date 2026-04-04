package com.library.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BookReview", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"BookID", "StudentID"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"book", "student"})
public class BookReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewID")
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookID", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentID", nullable = false)
    private Student student;

    @Column(name = "Rating", nullable = false)
    private Integer rating;

    @Column(name = "Comment", length = 500)
    private String comment;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;
}
