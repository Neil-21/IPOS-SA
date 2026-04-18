package com.infopharma.ipos_sa.repository;

/**
 * MonthlyDiscountRepository
 * Spring Data JPA repository for {@link com.infopharma.ipos_sa.entity.MonthlyDiscount}.
 * Uses {@code @EntityGraph} on the list-all and find-unsettled queries to
 * eagerly load the {@code account} association, preventing
 * LazyInitializationException during JSON serialisation with open-in-view disabled.
 */
import com.infopharma.ipos_sa.entity.MonthlyDiscount;
import com.infopharma.ipos_sa.entity.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyDiscountRepository extends JpaRepository<MonthlyDiscount,Integer> {
    List<MonthlyDiscount> findByAccount(UserAccount account);
    Optional<MonthlyDiscount> findByAccountAndMonthYear(UserAccount account, LocalDate monthYear);

    // Eager-fetch account so Jackson can serialise the merchant info after the
    // transaction closes (open-in-view=false). Without this we hit
    // LazyInitializationException on the JSON response.
    @EntityGraph(attributePaths = "account")
    List<MonthlyDiscount> findBySettledFalse();

    @EntityGraph(attributePaths = "account")
    @Override
    List<MonthlyDiscount> findAll();
}
