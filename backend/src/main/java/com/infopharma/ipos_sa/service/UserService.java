package com.infopharma.ipos_sa.service;

/**
 * UserService
 * Business logic contract for managing {@link com.infopharma.ipos_sa.entity.UserAccount}
 * records (both merchant and staff accounts). Includes credit-limit and
 * discount-plan assignment helpers, and a cascade-delete operation that
 * removes all related payments, invoices, and orders before deleting the
 * account itself. Also exposes {@code updateAllMerchantStatuses()} which is
 * called nightly by the {@link com.infopharma.ipos_sa.service.impl.AccountScheduler}.
 */
import com.infopharma.ipos_sa.entity.UserAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserService {

    UserAccount createAccount(UserAccount account);

    UserAccount updateAccount(UserAccount account);

    /** Cascades: deletes payments → invoices → orders then the account */
    void deleteAccount(Long id);

    Optional<UserAccount> findOne(Long id);

    List<UserAccount> findAll();

    UserAccount updateDiscountPlan(Long accountId, Integer discountPlanId);

    UserAccount updateCreditLimit(Long accountId, BigDecimal creditLimit);

    void updateAllMerchantStatuses();
}
