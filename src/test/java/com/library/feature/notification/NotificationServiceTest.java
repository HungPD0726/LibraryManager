package com.library.feature.notification;

import com.library.domain.model.Notification;
import com.library.domain.model.Student;
import com.library.domain.repository.NotificationRepository;
import com.library.domain.repository.StudentRepository;
import com.library.shared.constant.NotificationType;
import com.library.shared.support.NotificationTextSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private StudentRepository studentRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                studentRepository,
                new NotificationTextSupport());
    }

    @Test
    void create_shouldPersistCanonicalTextForKnownNotificationTypes() {
        Student student = new Student();
        student.setStudentId(8);

        when(studentRepository.findById(8)).thenReturn(Optional.of(student));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification created = notificationService.create(
                8,
                "Đơn mua đã giao",
                "Ðơn mua #18 đã được giao thành công.",
                NotificationType.ORDER_DELIVERED);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        assertThat(notificationCaptor.getValue().getTitle()).isEqualTo("Đơn mua đã giao");
        assertThat(notificationCaptor.getValue().getMessage()).isEqualTo("Đơn mua #18 đã được giao thành công.");
        assertThat(created.getTitle()).isEqualTo("Đơn mua đã giao");
        assertThat(created.getMessage()).isEqualTo("Đơn mua #18 đã được giao thành công.");
    }

    @Test
    void findByStudent_shouldReturnNormalizedCopiesWithoutMutatingLoadedEntities() {
        Notification stored = new Notification();
        stored.setNotificationId(30);
        stored.setStudent(new Student());
        stored.setTitle("Sách đã được trả");
        stored.setMessage("Ðơn muợn #30 đã được xác nhận trả thành công.");
        stored.setType(NotificationType.BORROW_RETURNED);
        stored.setIsRead(false);
        stored.setCreatedDate(LocalDateTime.of(2026, 4, 5, 16, 30));

        when(notificationRepository.findByStudentStudentIdOrderByCreatedDateDesc(9))
                .thenReturn(List.of(stored));

        List<Notification> notifications = notificationService.findByStudent(9);

        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getTitle()).isEqualTo("Sách đã được trả");
        assertThat(notifications.get(0).getMessage()).isEqualTo("Đơn mượn #30 đã được xác nhận trả thành công.");
        assertThat(stored.getTitle()).isEqualTo("Sách đã được trả");
        assertThat(stored.getMessage()).isEqualTo("Ðơn muợn #30 đã được xác nhận trả thành công.");
    }
}
