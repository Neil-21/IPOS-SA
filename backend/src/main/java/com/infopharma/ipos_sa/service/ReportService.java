package com.infopharma.ipos_sa.service;

/**
 * ReportService
 * Business logic contract for all report generation. Implementations query
 * the repository layer across date ranges and assemble the DTOs returned by
 * {@code /api/reports/*} endpoints:
 *   • Turnover report        — revenue and units sold per catalogue item
 *   • Merchant summary       — order count and total value for one merchant
 *   • Merchant detailed      — full line-item breakdown for one merchant
 *   • Invoice report         — invoices (all or per merchant) for a period
 *   • Stock turnover         — opening/closing stock per item
 *   • Debtor reminders       — merchants with overdue invoices
 */
import com.infopharma.ipos_sa.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    TurnoverReport getTurnoverReport(LocalDate from, LocalDate to);
    MerchantOrdersSummary getMerchantOrdersSummary(Long accountId, LocalDate from, LocalDate to);
    DetailedOrderReport getMerchantDetailedOrders(Long accountId, LocalDate from, LocalDate to);
    List<com.infopharma.ipos_sa.entity.Invoice> getMerchantInvoices(Long accountId, LocalDate from, LocalDate to);
    List<com.infopharma.ipos_sa.entity.Invoice> getAllInvoices(LocalDate from, LocalDate to);
    StockTurnoverReport getStockTurnoverReport(LocalDate from, LocalDate to);
    /** Debtors whose paymentDueDate has passed — shown on-screen as reminders */
    List<DebtorReminderItem> getDebtorReminders();
}
