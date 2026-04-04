package com.library.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Author")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AuthorID")
    private Integer authorId;

    @Column(name = "AuthorName", nullable = false, length = 100)
    private String authorName;
}
