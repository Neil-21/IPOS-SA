package com.infopharma.ipos_sa.dto;

/**
 * PaymentRequest
 * DTO for recording a payment against a merchant invoice. Carries the
 * account ID, invoice ID, amount paid, payment method
 * (BANK_TRANSFER | CARD | CHEQUE), the staff member entering the payment,
 * and an optional reference number.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infopharma.ipos_sa.entity.Payment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest {
    private Long accountId;
    private String invoiceId;
    private BigDecimal amountPaid;
    private Payment.PaymentMethod paymentMethod;
    private String recordedBy;
}
