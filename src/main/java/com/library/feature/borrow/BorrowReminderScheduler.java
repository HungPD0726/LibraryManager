package com.library.feature.borrow;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.borrow-reminders", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BorrowReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(BorrowReminderScheduler.class);

    private final BorrowReminderService borrowReminderService;

    @Scheduled(cron = "${app.borrow-reminders.cron:0 0 7 * * *}")
    public void runDailyReminders() {
        BorrowReminderService.BorrowReminderRunResult result = borrowReminderService.runDailyReminders();
        log.info(
                "Borrow reminder job finished: dueSoon={}, overdue={}, emailSkipped={}, emailFailures={}, failedRecords={}",
                result.dueSoonReminders(),
                result.overdueReminders(),
                result.emailSkipped(),
                result.emailFailures(),
                result.failedRecords()
        );
    }
}
