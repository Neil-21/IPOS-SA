package com.infopharma.ipos_sa.service;

/**
 * DiscountPlanService
 * Business logic contract for managing discount plans. Implementations handle
 * creation and full replacement of FIXED or FLEXIBLE discount plans, including
 * replacing all tier entries on an update.
 */
import com.infopharma.ipos_sa.dto.DiscountPlanRequest;
import com.infopharma.ipos_sa.entity.DiscountPlan;

import java.util.List;
import java.util.Optional;

public interface DiscountPlanService {
    DiscountPlan create(DiscountPlanRequest request);
    DiscountPlan update(Integer id, DiscountPlanRequest request);
    void delete(Integer id);
    Optional<DiscountPlan> findById(Integer id);
    List<DiscountPlan> findAll();
}
