package com.group6.librarymanager.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Book")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookID")
    private Integer bookId;

    @Column(name = "BookName", nullable = false, length = 200)
    private String bookName;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "Available", nullable = false)
    private Integer available;

    @ManyToOne
    @JoinColumn(name = "CategoryID", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "PublisherID", nullable = false)
    private Publisher publisher;

    @ManyToMany
    @JoinTable(name = "BookAuthor", joinColumns = @JoinColumn(name = "BookID"), inverseJoinColumns = @JoinColumn(name = "AuthorID"))
    private List<Author> authors;
}
