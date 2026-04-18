package com.infopharma.ipos_sa.service.impl;

/**
 * AccountScheduler
 * Spring {@code @Component} that runs a scheduled task at midnight every day
 * (cron {@code 0 0 0 * * *}) to flag merchant accounts as {@code IN_DEFAULT}
 * when any of their invoices have passed their payment due date.
 * Requires {@code @EnableScheduling} on the main application class.
 */
import com.infopharma.ipos_sa.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AccountScheduler {

    private final UserService userService;

    public AccountScheduler(UserService userService) {
        this.userService = userService;
    }

    // Runs every midnight — updates merchant statuses based on payment due date
    @Scheduled(cron = "0 0 0 * * *")
    public void runNightlyStatusUpdates() {
        userService.updateAllMerchantStatuses();
    }
}
