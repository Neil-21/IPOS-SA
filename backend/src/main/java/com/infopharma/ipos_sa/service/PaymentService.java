package com.infopharma.ipos_sa.service;

/**
 * PaymentService
 * Business logic contract for recording payments and querying invoices.
 * Recording a payment transitions the linked invoice's
 * {@code paymentStatus} from {@code PENDING} (or {@code OVERDUE}) to
 * {@code RECEIVED} and updates the merchant's running balance.
 */
import com.infopharma.ipos_sa.dto.PaymentRequest;
import com.infopharma.ipos_sa.entity.Invoice;
import com.infopharma.ipos_sa.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Payment recordPayment(PaymentRequest request);
    Optional<Invoice> findInvoiceById(String invoiceId);
    List<Invoice> findAllInvoices();
    List<Invoice> findInvoicesByAccountId(Long accountId);
}
