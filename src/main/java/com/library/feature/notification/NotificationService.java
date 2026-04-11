package com.library.feature.notification;

import com.library.domain.model.Notification;
import com.library.domain.model.Student;
import com.library.domain.repository.NotificationRepository;
import com.library.domain.repository.StudentRepository;
import com.library.shared.support.NotificationTextSupport;
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
    private final NotificationTextSupport notificationTextSupport;

    @Transactional
    public Notification create(Integer studentId, String title, String message, String type) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên ID: " + studentId));
        NotificationTextSupport.NotificationText normalized = notificationTextSupport.normalize(type, title, message);

        Notification notification = new Notification();
        notification.setStudent(student);
        notification.setTitle(normalized.title());
        notification.setMessage(normalized.message());
        notification.setType(normalized.type());
        notification.setIsRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> findByStudent(Integer studentId) {
        return notificationRepository.findByStudentStudentIdOrderByCreatedDateDesc(studentId)
                .stream()
                .map(notificationTextSupport::normalize)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(Integer studentId) {
        return notificationRepository.countUnreadByStudentId(studentId);
    }

    @Transactional
    public void markRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo."));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(Integer studentId) {
        notificationRepository.markAllReadByStudentId(studentId);
    }
}
