package com.library.feature.notification;

import com.library.domain.model.Notification;
import com.library.domain.model.Student;
import com.library.domain.repository.NotificationRepository;
import com.library.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public Notification create(Integer studentId, String title, String message, String type) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y sinh viÃƒÂªn ID: " + studentId));

        Notification notification = new Notification();
        notification.setStudent(student);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> findByStudent(Integer studentId) {
        return notificationRepository.findByStudentStudentIdOrderByCreatedDateDesc(studentId);
    }

    @Transactional(readOnly = true)
    public long countUnread(Integer studentId) {
        return notificationRepository.countUnreadByStudentId(studentId);
    }

    @Transactional
    public void markRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y thÃƒÂ´ng bÃƒÂ¡o."));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(Integer studentId) {
        notificationRepository.markAllReadByStudentId(studentId);
    }
}
