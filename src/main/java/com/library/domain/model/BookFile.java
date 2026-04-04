package com.library.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BookFile")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString
public class BookFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookFileID")
    private Integer bookFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookID", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @PrePersist
    void applyDefaults() {
        if (uploadAt == null) {
            uploadAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }
    }
}
