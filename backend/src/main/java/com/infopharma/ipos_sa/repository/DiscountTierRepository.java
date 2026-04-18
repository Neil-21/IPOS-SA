package com.infopharma.ipos_sa.repository;

/**
 * DiscountTierRepository
 * Spring Data JPA repository for {@link com.infopharma.ipos_sa.entity.DiscountTier}.
 * Used by {@link com.infopharma.ipos_sa.service.impl.DiscountPlanServiceImpl}
 * to delete all tiers belonging to a plan before replacing them on an update.
 */
import com.infopharma.ipos_sa.entity.DiscountTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountTierRepository  extends JpaRepository<DiscountTier,Integer> {
}
