package com.group6.librarymanager.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "BookFile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookFileID")
    private Integer bookFileId;

    @ManyToOne
    @JoinColumn(name = "BookID", nullable = false)
    private Book book;

    @ManyToOne
    @JoinColumn(name = "StaffID", nullable = false)
    private Staff staff;

    @Column(name = "FileName", nullable = false, length = 255)
    private String fileName;

    @Column(name = "FileUrl", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "FileType", length = 50)
    private String fileType;

    @Column(name = "FileSize")
    private Long fileSize;

    @Column(name = "UploadAt", nullable = false)
    private LocalDateTime uploadAt;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
}
