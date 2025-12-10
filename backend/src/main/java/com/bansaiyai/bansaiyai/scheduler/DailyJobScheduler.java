package com.bansaiyai.bansaiyai.scheduler;

import com.bansaiyai.bansaiyai.service.AccountingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyJobScheduler {

    private final AccountingService accountingService;

    /**
     * Run daily tasks at 00:01 AM every day.
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void runDailyProcessing() {
        log.info("Starting Daily Job Processing at {}", LocalDateTime.now());

        try {
            // Task 1: Check for overdue loans and update status (if needed)
            accountingService.checkAndFlagOverdueLoans();

            // Task 2: Daily Interest Accrual (Optional, depending on business rule)
            // accountingService.runDailyInterestAccrual();

            log.info("Daily Job Processing completed successfully.");
        } catch (Exception e) {
            log.error("Error during Daily Job Processing", e);
        }
    }
}
