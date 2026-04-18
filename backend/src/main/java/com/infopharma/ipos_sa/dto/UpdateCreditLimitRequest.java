package com.infopharma.ipos_sa.dto;

/**
 * UpdateCreditLimitRequest
 * DTO for updating the credit limit of a merchant account.
 * Used by {@code PATCH /api/accounts/{id}/credit-limit}.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCreditLimitRequest {
    private BigDecimal creditLimit;
}
