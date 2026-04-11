package com.library.feature.borrow;

import com.library.domain.model.Borrow;
import com.library.domain.model.Notification;
import com.library.domain.model.Student;
import com.library.domain.repository.BorrowRepository;
import com.library.feature.auth.EmailService;
import com.library.feature.auth.EmailTemplateService;
import com.library.feature.notification.NotificationService;
import com.library.shared.constant.BorrowStatus;
import com.library.shared.constant.NotificationType;
import com.library.shared.support.NotificationTextSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowReminderServiceTest {

    @Mock
    private BorrowRepository borrowRepository;

    private RecordingNotificationService notificationService;
    private RecordingEmailService emailService;
    private EmailTemplateService emailTemplateService;
    private BorrowReminderService borrowReminderService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-11T00:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        notificationService = new RecordingNotificationService();
        emailService = new RecordingEmailService();
        emailTemplateService = new EmailTemplateService();
        borrowReminderService = new BorrowReminderService(
                borrowRepository,
                notificationService,
                emailService,
                emailTemplateService,
                new NoOpTransactionManager(),
                fixedClock
        );
    }

    @Test
    void runDailyReminders_shouldCreateDueSoonNotificationAndSendEmail() {
        Borrow borrow = borrow(41, LocalDate.of(2026, 4, 12), BorrowStatus.BORROWING, "minh@example.com");

        when(borrowRepository.findDueSoonReminderCandidates(BorrowStatus.BORROWING, LocalDate.of(2026, 4, 12)))
                .thenReturn(List.of(borrow));
        when(borrowRepository.findOverdueReminderCandidates(List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE), LocalDate.of(2026, 4, 11)))
                .thenReturn(List.of());
        when(borrowRepository.markDueReminderSent(41, BorrowStatus.BORROWING, LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 11)))
                .thenReturn(1);

        BorrowReminderService.BorrowReminderRunResult result = borrowReminderService.runDailyReminders();

        assertThat(notificationService.records()).hasSize(1);
        RecordedNotification notification = notificationService.records().get(0);
        assertThat(notification.student()).isSameAs(borrow.getStudent());
        assertThat(notification.type()).isEqualTo(NotificationType.BORROW_DUE_SOON);
        assertThat(notification.title()).isEqualTo("Sắp đến hạn trả sách");
        assertThat(notification.message()).contains("#41").contains("12/04/2026");

        assertThat(emailService.sentEmails()).hasSize(1);
        SentEmail sentEmail = emailService.sentEmails().get(0);
        assertThat(sentEmail.to()).isEqualTo("minh@example.com");
        assertThat(sentEmail.subject()).isEqualTo("Nhắc hạn trả sách");
        assertThat(sentEmail.html()).contains("#41").contains("12/04/2026").contains("Sinh viên 41");

        assertThat(result.dueSoonReminders()).isEqualTo(1);
        assertThat(result.overdueReminders()).isZero();
        assertThat(result.emailSkipped()).isZero();
        assertThat(result.emailFailures()).isZero();
        assertThat(result.failedRecords()).isZero();
    }

    @Test
    void runDailyReminders_shouldNotDispatchDueSoonReminderTwice() {
        Borrow borrow = borrow(51, LocalDate.of(2026, 4, 12), BorrowStatus.BORROWING, "minh@example.com");

        when(borrowRepository.findDueSoonReminderCandidates(BorrowStatus.BORROWING, LocalDate.of(2026, 4, 12)))
                .thenReturn(List.of(borrow));
        when(borrowRepository.findOverdueReminderCandidates(List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE), LocalDate.of(2026, 4, 11)))
                .thenReturn(List.of());
        when(borrowRepository.markDueReminderSent(51, BorrowStatus.BORROWING, LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 11)))
                .thenReturn(0);

        BorrowReminderService.BorrowReminderRunResult result = borrowReminderService.runDailyReminders();

        assertThat(notificationService.records()).isEmpty();
        assertThat(emailService.sentEmails()).isEmpty();
        assertThat(result.dueSoonReminders()).isZero();
        assertThat(result.overdueReminders()).isZero();
    }

    @Test
    void runDailyReminders_shouldMarkBorrowOverdueCreateNotificationAndSendEmail() {
        Borrow borrow = borrow(61, LocalDate.of(2026, 4, 9), BorrowStatus.BORROWING, "minh@example.com");

        when(borrowRepository.findDueSoonReminderCandidates(BorrowStatus.BORROWING, LocalDate.of(2026, 4, 12)))
                .thenReturn(List.of());
        when(borrowRepository.findOverdueReminderCandidates(List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE), LocalDate.of(2026, 4, 11)))
                .thenReturn(List.of(borrow));
        when(borrowRepository.markOverdueReminderSent(
                61,
                List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE),
                BorrowStatus.OVERDUE,
                LocalDate.of(2026, 4, 11),
                LocalDate.of(2026, 4, 11)
        )).thenReturn(1);

        BorrowReminderService.BorrowReminderRunResult result = borrowReminderService.runDailyReminders();

        assertThat(notificationService.records()).hasSize(1);
        RecordedNotification notification = notificationService.records().get(0);
        assertThat(notification.type()).isEqualTo(NotificationType.BORROW_OVERDUE);
        assertThat(notification.title()).isEqualTo("Phiếu mượn đã quá hạn");
        assertThat(notification.message()).contains("#61").contains("09/04/2026").contains("2 ngày");

        assertThat(emailService.sentEmails()).hasSize(1);
        SentEmail sentEmail = emailService.sentEmails().get(0);
        assertThat(sentEmail.to()).isEqualTo("minh@example.com");
        assertThat(sentEmail.subject()).isEqualTo("Phiếu mượn đã quá hạn");
        assertThat(sentEmail.html()).contains("#61").contains("09/04/2026").contains("2 ngày");

        assertThat(result.dueSoonReminders()).isZero();
        assertThat(result.overdueReminders()).isEqualTo(1);
        assertThat(result.emailSkipped()).isZero();
        assertThat(result.emailFailures()).isZero();
        assertThat(result.failedRecords()).isZero();
    }

    @Test
    void runDailyReminders_shouldSkipEmailButKeepNotificationWhenEmailIsUnavailable() {
        Borrow borrow = borrow(71, LocalDate.of(2026, 4, 12), BorrowStatus.BORROWING, "");

        when(borrowRepository.findDueSoonReminderCandidates(BorrowStatus.BORROWING, LocalDate.of(2026, 4, 12)))
                .thenReturn(List.of(borrow));
        when(borrowRepository.findOverdueReminderCandidates(List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE), LocalDate.of(2026, 4, 11)))
                .thenReturn(List.of());
        when(borrowRepository.markDueReminderSent(71, BorrowStatus.BORROWING, LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 11)))
                .thenReturn(1);

        BorrowReminderService.BorrowReminderRunResult result = borrowReminderService.runDailyReminders();

        assertThat(notificationService.records()).hasSize(1);
        assertThat(notificationService.records().get(0).type()).isEqualTo(NotificationType.BORROW_DUE_SOON);
        assertThat(emailService.sentEmails()).isEmpty();
        assertThat(result.dueSoonReminders()).isEqualTo(1);
        assertThat(result.emailSkipped()).isEqualTo(1);
        assertThat(result.emailFailures()).isZero();
    }

    @Test
    void runDailyReminders_shouldContinueProcessingWhenOneEmailFails() {
        Borrow firstBorrow = borrow(81, LocalDate.of(2026, 4, 12), BorrowStatus.BORROWING, "a@example.com");
        Borrow secondBorrow = borrow(82, LocalDate.of(2026, 4, 12), BorrowStatus.BORROWING, "b@example.com");
        emailService.failFor("a@example.com");

        when(borrowRepository.findDueSoonReminderCandidates(BorrowStatus.BORROWING, LocalDate.of(2026, 4, 12)))
                .thenReturn(List.of(firstBorrow, secondBorrow));
        when(borrowRepository.findOverdueReminderCandidates(List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE), LocalDate.of(2026, 4, 11)))
                .thenReturn(List.of());
        when(borrowRepository.markDueReminderSent(81, BorrowStatus.BORROWING, LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 11)))
                .thenReturn(1);
        when(borrowRepository.markDueReminderSent(82, BorrowStatus.BORROWING, LocalDate.of(2026, 4, 12), LocalDate.of(2026, 4, 11)))
                .thenReturn(1);

        BorrowReminderService.BorrowReminderRunResult result = borrowReminderService.runDailyReminders();

        assertThat(notificationService.records()).hasSize(2);
        assertThat(emailService.sentEmails()).hasSize(1);
        assertThat(emailService.sentEmails().get(0).to()).isEqualTo("b@example.com");
        assertThat(result.dueSoonReminders()).isEqualTo(2);
        assertThat(result.emailFailures()).isEqualTo(1);
        assertThat(result.failedRecords()).isZero();
    }

    @Test
    void runDailyReminders_shouldIgnoreNonBorrowingStatuses() {
        when(borrowRepository.findDueSoonReminderCandidates(BorrowStatus.BORROWING, LocalDate.of(2026, 4, 12)))
                .thenReturn(List.of());
        when(borrowRepository.findOverdueReminderCandidates(List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE), LocalDate.of(2026, 4, 11)))
                .thenReturn(List.of());

        BorrowReminderService.BorrowReminderRunResult result = borrowReminderService.runDailyReminders();

        verify(borrowRepository, never()).markDueReminderSent(anyInt(), eq(BorrowStatus.BORROWING), eq(LocalDate.of(2026, 4, 12)), eq(LocalDate.of(2026, 4, 11)));
        verify(borrowRepository, never()).markOverdueReminderSent(anyInt(), anyCollection(), eq(BorrowStatus.OVERDUE), eq(LocalDate.of(2026, 4, 11)), eq(LocalDate.of(2026, 4, 11)));
        assertThat(notificationService.records()).isEmpty();
        assertThat(emailService.sentEmails()).isEmpty();
        assertThat(result.dueSoonReminders()).isZero();
        assertThat(result.overdueReminders()).isZero();
    }

    private static Borrow borrow(int borrowId, LocalDate dueDate, String status, String email) {
        Student student = new Student();
        student.setStudentId(borrowId);
        student.setStudentName("Sinh viên " + borrowId);
        student.setEmail(email);

        Borrow borrow = new Borrow();
        borrow.setBorrowId(borrowId);
        borrow.setStudent(student);
        borrow.setDueDate(dueDate);
        borrow.setStatus(status);
        return borrow;
    }

    private static final class NoOpTransactionManager implements PlatformTransactionManager {

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
        }

        @Override
        public void rollback(TransactionStatus status) {
        }
    }

    private static final class RecordingNotificationService extends NotificationService {

        private final List<RecordedNotification> records = new ArrayList<>();

        private RecordingNotificationService() {
            super(null, null, new NotificationTextSupport());
        }

        @Override
        public Notification create(Student student, String title, String message, String type) {
            NotificationTextSupport.NotificationText normalized = new NotificationTextSupport().normalize(type, title, message);
            records.add(new RecordedNotification(student, normalized.title(), normalized.message(), normalized.type()));

            Notification notification = new Notification();
            notification.setStudent(student);
            notification.setTitle(normalized.title());
            notification.setMessage(normalized.message());
            notification.setType(normalized.type());
            return notification;
        }

        private List<RecordedNotification> records() {
            return records;
        }
    }

    private static final class RecordingEmailService extends EmailService {

        private final List<SentEmail> sentEmails = new ArrayList<>();
        private boolean configured = true;
        private String failingRecipient;

        private RecordingEmailService() {
            super(new NoOpMailSender(), new EmailTemplateService());
        }

        @Override
        public boolean isConfigured() {
            return configured;
        }

        @Override
        public void sendHtml(String toEmail, String subject, String htmlContent) {
            if (!configured) {
                throw new IllegalStateException("mail not configured");
            }
            if (toEmail.equals(failingRecipient)) {
                throw new IllegalStateException("smtp down");
            }
            sentEmails.add(new SentEmail(toEmail, subject, htmlContent));
        }

        private void failFor(String recipient) {
            this.failingRecipient = recipient;
        }

        private List<SentEmail> sentEmails() {
            return sentEmails;
        }
    }

    private record RecordedNotification(Student student, String title, String message, String type) {
    }

    private record SentEmail(String to, String subject, String html) {
    }

    private static final class NoOpMailSender implements JavaMailSender {

        @Override
        public jakarta.mail.internet.MimeMessage createMimeMessage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public jakarta.mail.internet.MimeMessage createMimeMessage(java.io.InputStream contentStream) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void send(jakarta.mail.internet.MimeMessage mimeMessage) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void send(jakarta.mail.internet.MimeMessage... mimeMessages) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void send(org.springframework.mail.SimpleMailMessage simpleMessage) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void send(org.springframework.mail.SimpleMailMessage... simpleMessages) {
            throw new UnsupportedOperationException();
        }
    }
}
