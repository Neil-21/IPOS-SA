package com.infopharma.ipos_sa.dto;

/**
 * DiscountPlanRequest
 * DTO for creating or updating a discount plan. Specifies the plan type
 * (FIXED or FLEXIBLE) and, for flexible plans, a list of {@code TierRequest}
 * objects defining the value thresholds and corresponding discount rates.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infopharma.ipos_sa.entity.DiscountPlan;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscountPlanRequest {

    private DiscountPlan.PlanType planType;
    private List<TierRequest> tiers;

    @Data
    @NoArgsConstructor
    public static class TierRequest {
        private BigDecimal minValue;
        private BigDecimal maxValue;    // null = no upper bound (top tier)
        private BigDecimal discountRate;
    }
}
