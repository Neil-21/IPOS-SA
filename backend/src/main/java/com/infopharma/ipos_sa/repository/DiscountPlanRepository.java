package com.infopharma.ipos_sa.repository;

/**
 * DiscountPlanRepository
 * Spring Data JPA repository for {@link com.infopharma.ipos_sa.entity.DiscountPlan}.
 * Uses all inherited {@code JpaRepository} methods — no custom queries needed
 * as plans are fetched by ID or as a full list.
 */
import com.infopharma.ipos_sa.entity.DiscountPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountPlanRepository extends JpaRepository<DiscountPlan,Integer> {
}
