package com.infopharma.ipos_sa.dto;

/**
 * DebtorReminderItem
 * DTO representing a merchant with an overdue payment balance, used by the
 * debtor report and the frontend Reminders page. Includes account details
 * and the outstanding balance amount.
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebtorReminderItem {
    private Long accountId;
    private String companyName;
    private String contactName;
    private String email;
    private BigDecimal balance;
    private LocalDate paymentDueDate;
    private long daysOverdue;
    private String accountStatus;
}
