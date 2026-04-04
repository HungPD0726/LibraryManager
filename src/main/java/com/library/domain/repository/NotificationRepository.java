package com.library.domain.repository;

import com.library.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByStudentStudentIdOrderByCreatedDateDesc(Integer studentId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.student.studentId = :studentId AND n.isRead = false")
    long countUnreadByStudentId(@Param("studentId") Integer studentId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.student.studentId = :studentId AND n.isRead = false")
    int markAllReadByStudentId(@Param("studentId") Integer studentId);
}
