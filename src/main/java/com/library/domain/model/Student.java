package com.library.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Student")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StudentID")
    private Integer studentId;

    @Column(name = "StudentName", nullable = false, length = 100)
    private String studentName;

    @Column(name = "Email", length = 100, unique = true)
    private String email;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Transient
    private String avatarUrl;

    @Transient
    private String className;

    @Transient
    private String facultyName;

    @Transient
    private String accountStatus;

    @Transient
    private LocalDateTime createdAt;
}
