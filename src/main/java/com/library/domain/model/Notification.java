package com.library.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"student"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Integer notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentID", nullable = false)
    private Student student;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Message", length = 1000)
    private String message;

    @Column(name = "Type", length = 50)
    private String type;

    @Column(name = "IsRead", nullable = false)
    private Boolean isRead = false;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;
}
