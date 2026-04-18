package com.infopharma.ipos_sa.dto;

/**
 * UpdateDiscountPlanAssignRequest
 * DTO for assigning (or reassigning) a discount plan to a merchant account.
 * Carries the target {@code discountPlanId}. Used by
 * {@code PATCH /api/accounts/{id}/discount-plan}.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateDiscountPlanAssignRequest {
    private Integer discountPlanId;
}
