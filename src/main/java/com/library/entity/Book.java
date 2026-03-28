package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Book")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"category", "publisher", "authors"})
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PublisherID")
    private Publisher publisher;

    @Column(name = "Description", length = 1000)
    private String description;

    @Column(name = "ShelfLocation", length = 200)
    private String shelfLocation;

    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "BookAuthor",
        joinColumns = @JoinColumn(name = "BookID"),
        inverseJoinColumns = @JoinColumn(name = "AuthorID")
    )
    private Set<Author> authors = new HashSet<>();
}
