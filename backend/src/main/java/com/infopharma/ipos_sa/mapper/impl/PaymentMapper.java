package com.infopharma.ipos_sa.mapper.impl;

/**
 * PaymentMapper
 * Maps between a {@link com.infopharma.ipos_sa.entity.Payment} entity and a
 * {@link com.infopharma.ipos_sa.dto.PaymentRequest} DTO. STRICT matching
 * means accountId and invoiceId (no direct field match) are skipped and
 * must be set manually in the service layer.
 */
import com.infopharma.ipos_sa.dto.PaymentRequest;
import com.infopharma.ipos_sa.entity.Payment;
import com.infopharma.ipos_sa.mapper.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper implements Mapper<Payment, PaymentRequest> {

    private final ModelMapper modelMapper;

    public PaymentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public PaymentRequest mapTo(Payment payment) {
        return modelMapper.map(payment, PaymentRequest.class);
    }

    @Override
    public Payment mapFrom(PaymentRequest paymentRequest) {
        // Maps amountPaid, paymentMethod, recordedBy — STRICT skips accountId/invoiceId (no match)
        return modelMapper.map(paymentRequest, Payment.class);
    }
}
