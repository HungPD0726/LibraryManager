package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Publisher")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PublisherID")
    private Integer publisherId;

    @Column(name = "PublisherName", nullable = false, length = 100, unique = true)
    private String publisherName;
}
