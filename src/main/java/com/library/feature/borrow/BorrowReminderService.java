package com.library.feature.borrow;

import com.library.domain.model.Borrow;
import com.library.domain.model.Student;
import com.library.domain.repository.BorrowRepository;
import com.library.feature.auth.EmailService;
import com.library.feature.auth.EmailTemplateService;
import com.library.feature.notification.NotificationService;
import com.library.shared.constant.BorrowStatus;
import com.library.shared.constant.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BorrowReminderService {

    private static final Logger log = LoggerFactory.getLogger(BorrowReminderService.class);

    private final BorrowRepository borrowRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public BorrowReminderService(BorrowRepository borrowRepository,
                                 NotificationService notificationService,
                                 EmailService emailService,
                                 EmailTemplateService emailTemplateService,
                                 PlatformTransactionManager transactionManager,
                                 Clock clock) {
        this.borrowRepository = borrowRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
        this.clock = clock;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public BorrowReminderRunResult runDailyReminders() {
        LocalDate today = LocalDate.now(clock);
        LocalDate reminderDate = today.plusDays(1);

        List<Borrow> dueSoonCandidates = borrowRepository.findDueSoonReminderCandidates(BorrowStatus.BORROWING, reminderDate);
        List<Borrow> overdueCandidates = borrowRepository.findOverdueReminderCandidates(
                List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE),
                today
        );

        BorrowReminderCounters counters = new BorrowReminderCounters();
        dueSoonCandidates.forEach(borrow -> processCandidate(() -> dispatchDueSoonReminder(borrow, today), counters));
        overdueCandidates.forEach(borrow -> processCandidate(() -> dispatchOverdueReminder(borrow, today), counters));

        return counters.toResult();
    }

    private void processCandidate(ReminderAction action, BorrowReminderCounters counters) {
        try {
            ReminderDispatchResult result = transactionTemplate.execute(status -> action.run());
            if (result == null) {
                return;
            }

            counters.add(result);
        } catch (RuntimeException ex) {
            counters.failedRecords++;
            log.error("Không thể xử lý reminder phiếu mượn.", ex);
        }
    }

    private ReminderDispatchResult dispatchDueSoonReminder(Borrow borrow, LocalDate today) {
        int updated = borrowRepository.markDueReminderSent(
                borrow.getBorrowId(),
                BorrowStatus.BORROWING,
                today.plusDays(1),
                today
        );
        if (updated == 0) {
            return ReminderDispatchResult.skipped();
        }

        String message = buildDueSoonMessage(borrow);
        notificationService.create(
                borrow.getStudent(),
                "Sắp đến hạn trả sách",
                message,
                NotificationType.BORROW_DUE_SOON
        );

        ReminderDispatchResult emailResult = sendDueSoonEmail(borrow);
        return emailResult.dispatchedAsDueSoon();
    }

    private ReminderDispatchResult dispatchOverdueReminder(Borrow borrow, LocalDate today) {
        int updated = borrowRepository.markOverdueReminderSent(
                borrow.getBorrowId(),
                List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE),
                BorrowStatus.OVERDUE,
                today,
                today
        );
        if (updated == 0) {
            return ReminderDispatchResult.skipped();
        }

        String message = buildOverdueMessage(borrow, today);
        notificationService.create(
                borrow.getStudent(),
                "Phiếu mượn đã quá hạn",
                message,
                NotificationType.BORROW_OVERDUE
        );

        ReminderDispatchResult emailResult = sendOverdueEmail(borrow, today);
        return emailResult.dispatchedAsOverdue();
    }

    private ReminderDispatchResult sendDueSoonEmail(Borrow borrow) {
        Student student = borrow.getStudent();
        if (!canSendEmail(student, borrow.getBorrowId())) {
            return ReminderDispatchResult.skippedEmail();
        }

        try {
            emailService.sendHtml(
                    student.getEmail(),
                    "Nhắc hạn trả sách",
                    emailTemplateService.buildBorrowDueSoonReminderTemplate(
                            resolveStudentName(student),
                            borrow.getBorrowId(),
                            borrow.getDueDate()
                    )
            );
            return ReminderDispatchResult.none();
        } catch (RuntimeException ex) {
            log.error("Không thể gửi email nhắc hạn cho phiếu mượn #{}.", borrow.getBorrowId(), ex);
            return ReminderDispatchResult.failedEmail();
        }
    }

    private ReminderDispatchResult sendOverdueEmail(Borrow borrow, LocalDate today) {
        Student student = borrow.getStudent();
        if (!canSendEmail(student, borrow.getBorrowId())) {
            return ReminderDispatchResult.skippedEmail();
        }

        long overdueDays = Math.max(1, ChronoUnit.DAYS.between(borrow.getDueDate(), today));

        try {
            emailService.sendHtml(
                    student.getEmail(),
                    "Phiếu mượn đã quá hạn",
                    emailTemplateService.buildBorrowOverdueReminderTemplate(
                            resolveStudentName(student),
                            borrow.getBorrowId(),
                            borrow.getDueDate(),
                            overdueDays
                    )
            );
            return ReminderDispatchResult.none();
        } catch (RuntimeException ex) {
            log.error("Không thể gửi email quá hạn cho phiếu mượn #{}.", borrow.getBorrowId(), ex);
            return ReminderDispatchResult.failedEmail();
        }
    }

    private boolean canSendEmail(Student student, Integer borrowId) {
        if (student == null) {
            log.warn("Bỏ qua email reminder cho phiếu mượn #{} vì không có thông tin sinh viên.", borrowId);
            return false;
        }
        if (!StringUtils.hasText(student.getEmail())) {
            log.warn("Bỏ qua email reminder cho phiếu mượn #{} vì sinh viên chưa có email.", borrowId);
            return false;
        }
        if (!emailService.isConfigured()) {
            log.warn("Bỏ qua email reminder cho phiếu mượn #{} vì hệ thống mail chưa được cấu hình.", borrowId);
            return false;
        }
        return true;
    }

    private String buildDueSoonMessage(Borrow borrow) {
        return "Đơn mượn #" + borrow.getBorrowId()
                + " sẽ đến hạn vào ngày " + emailTemplateService.formatDate(borrow.getDueDate())
                + ". Vui lòng sắp xếp trả sách đúng hạn.";
    }

    private String buildOverdueMessage(Borrow borrow, LocalDate today) {
        long overdueDays = Math.max(1, ChronoUnit.DAYS.between(borrow.getDueDate(), today));
        return "Đơn mượn #" + borrow.getBorrowId()
                + " đã quá hạn " + overdueDays + " ngày kể từ "
                + emailTemplateService.formatDate(borrow.getDueDate())
                + ". Vui lòng trả sách sớm để tránh phát sinh xử lý thêm.";
    }

    private String resolveStudentName(Student student) {
        if (student == null || !StringUtils.hasText(student.getStudentName())) {
            return "bạn";
        }
        return student.getStudentName().trim();
    }

    @FunctionalInterface
    private interface ReminderAction {
        ReminderDispatchResult run();
    }

    private static final class BorrowReminderCounters {
        private int dueSoonReminders;
        private int overdueReminders;
        private int emailSkipped;
        private int emailFailures;
        private int failedRecords;

        private void add(ReminderDispatchResult result) {
            dueSoonReminders += result.dueSoonProcessed;
            overdueReminders += result.overdueProcessed;
            emailSkipped += result.emailSkipped;
            emailFailures += result.emailFailures;
        }

        private BorrowReminderRunResult toResult() {
            return new BorrowReminderRunResult(
                    dueSoonReminders,
                    overdueReminders,
                    emailSkipped,
                    emailFailures,
                    failedRecords
            );
        }
    }

    private static final class ReminderDispatchResult {
        private final int dueSoonProcessed;
        private final int overdueProcessed;
        private final int emailSkipped;
        private final int emailFailures;

        private ReminderDispatchResult(int dueSoonProcessed, int overdueProcessed, int emailSkipped, int emailFailures) {
            this.dueSoonProcessed = dueSoonProcessed;
            this.overdueProcessed = overdueProcessed;
            this.emailSkipped = emailSkipped;
            this.emailFailures = emailFailures;
        }

        private static ReminderDispatchResult none() {
            return new ReminderDispatchResult(0, 0, 0, 0);
        }

        private static ReminderDispatchResult skipped() {
            return none();
        }

        private static ReminderDispatchResult skippedEmail() {
            return new ReminderDispatchResult(0, 0, 1, 0);
        }

        private static ReminderDispatchResult failedEmail() {
            return new ReminderDispatchResult(0, 0, 0, 1);
        }

        private ReminderDispatchResult dispatchedAsDueSoon() {
            return new ReminderDispatchResult(1, 0, emailSkipped, emailFailures);
        }

        private ReminderDispatchResult dispatchedAsOverdue() {
            return new ReminderDispatchResult(0, 1, emailSkipped, emailFailures);
        }
    }

    public record BorrowReminderRunResult(int dueSoonReminders,
                                          int overdueReminders,
                                          int emailSkipped,
                                          int emailFailures,
                                          int failedRecords) {
    }
}
