package com.infopharma.ipos_sa.service;

/**
 * UserServiceImplTest
 * Unit tests for {@link com.infopharma.ipos_sa.service.impl.UserServiceImpl}
 * using Mockito mocks. Verifies account creation, cascade deletion (payments
 * → invoices → orders → account), credit-limit updates, discount-plan
 * assignment, and the nightly {@code updateAllMerchantStatuses} logic.
 */
import com.infopharma.ipos_sa.entity.*;
import com.infopharma.ipos_sa.repository.*;
import com.infopharma.ipos_sa.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl.
 *
 * Covers creating and updating accounts, deleting accounts, updating
 * credit limits and discount plans and the scheduled status update
 * that suspends or defaults overdue merchants.
 *
 */

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserAccountRepository userAccountRepository;
    @Mock DiscountPlanRepository discountPlanRepository;
    @Mock OrderRepository orderRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock PaymentRepository paymentRepository;

    @InjectMocks UserServiceImpl userService;

    private UserAccount merchantAccount;

    @BeforeEach
    void setUp() {
        merchantAccount = new UserAccount();
        merchantAccount.setAccountId(1L);
        merchantAccount.setUsername("merchant1");
        merchantAccount.setAccountType(UserAccount.AccountType.MERCHANT);
        merchantAccount.setAccountStatus(UserAccount.AccountStatus.NORMAL);
        merchantAccount.setBalance(BigDecimal.ZERO);
        merchantAccount.setEmail("merchant@test.com");
        merchantAccount.setPhone("01234567890");
        merchantAccount.setPassword("secret");
        merchantAccount.setIsActive(true);
    }


    //   createAccount
    // Creating am account should save it and return the saved object
    @Test
    void createAccount_savesAndReturnsAccount() {
        when(userAccountRepository.save(merchantAccount)).thenReturn(merchantAccount);

        UserAccount result = userService.createAccount(merchantAccount);

        assertThat(result).isSameAs(merchantAccount);
        verify(userAccountRepository).save(merchantAccount);
    }


    //   updateAccount
    // Updating an account should save the changes and return the updated version
    @Test
    void updateAccount_savesAndReturnsAccount() {
        when(userAccountRepository.save(merchantAccount)).thenReturn(merchantAccount);

        UserAccount result = userService.updateAccount(merchantAccount);

        assertThat(result).isSameAs(merchantAccount);
        verify(userAccountRepository).save(merchantAccount);
    }


    //   deleteAccount
    // Deleting an account that doesn't exist should throw an error
    @Test
    void deleteAccount_accountNotFound_throwsEntityNotFoundException() {
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // Deleting an account should first remove its payments, invoices, and orders [respectively] before the account itself
    @Test
    void deleteAccount_success_deletesPaymentsInvoicesOrdersThenAccount() {
        Payment payment = new Payment();
        Invoice invoice = new Invoice();
        Order order = new Order();

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(merchantAccount));
        when(paymentRepository.findByAccount(merchantAccount)).thenReturn(List.of(payment));
        when(invoiceRepository.findByAccount(merchantAccount)).thenReturn(List.of(invoice));
        when(orderRepository.findByAccount(merchantAccount)).thenReturn(List.of(order));

        userService.deleteAccount(1L);

        verify(paymentRepository).deleteAll(List.of(payment));
        verify(invoiceRepository).deleteAll(List.of(invoice));
        verify(orderRepository).deleteAll(List.of(order));
        verify(userAccountRepository).deleteById(1L);
    }

    // Deleting an account (with no orders, invoices or payments) should still be deleted properly/work fine
    @Test
    void deleteAccount_noRelatedRecords_stillDeletesAccount() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(merchantAccount));
        when(paymentRepository.findByAccount(merchantAccount)).thenReturn(List.of());
        when(invoiceRepository.findByAccount(merchantAccount)).thenReturn(List.of());
        when(orderRepository.findByAccount(merchantAccount)).thenReturn(List.of());

        userService.deleteAccount(1L);

        verify(userAccountRepository).deleteById(1L);
    }


    //   findAccount
    // Looking up an account that exists should return it
    @Test
    void findOne_found_returnsAccount() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(merchantAccount));

        Optional<UserAccount> result = userService.findOne(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getAccountId()).isEqualTo(1L);
    }

    // Looking up an account ID that doesn't exist should return empty - and not an error
    @Test
    void findOne_notFound_returnsEmpty() {
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(userService.findOne(99L)).isEmpty();
    }


    //   findAll
    // Searching all accounts should return every account in the system
    @Test
    void findAll_returnsList() {
        UserAccount admin = new UserAccount();
        admin.setAccountId(2L);
        when(userAccountRepository.findAll()).thenReturn(List.of(merchantAccount, admin));

        List<UserAccount> result = userService.findAll();

        assertThat(result).hasSize(2);
    }


    //   updateDiscountPlan
    // Updating the discount plan to an account that doesn't exist should throw an error
    @Test
    void updateDiscountPlan_accountNotFound_throwsEntityNotFoundException() {
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateDiscountPlan(99L, 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // Assigning a discount plan ID that doesn't exist should throw an error
    @Test
    void updateDiscountPlan_planNotFound_throwsEntityNotFoundException() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(merchantAccount));
        when(discountPlanRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateDiscountPlan(1L, 99))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // Assigning a valid discount plan should link the plan to the account & save the changes
    @Test
    void updateDiscountPlan_success_assignsPlanAndSaves() {
        DiscountPlan plan = new DiscountPlan();
        plan.setDiscountPlanId(5);
        plan.setPlanType(DiscountPlan.PlanType.FIXED);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(merchantAccount));
        when(discountPlanRepository.findById(5)).thenReturn(Optional.of(plan));
        when(userAccountRepository.save(merchantAccount)).thenReturn(merchantAccount);

        UserAccount result = userService.updateDiscountPlan(1L, 5);

        assertThat(result.getDiscountPlan()).isSameAs(plan);
        verify(userAccountRepository).save(merchantAccount);
    }


    //   updateCreditLimit
    // Trying to update the credit limit for an account that doesn't exist should throw an error
    @Test
    void updateCreditLimit_accountNotFound_throwsEntityNotFoundException() {
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateCreditLimit(99L, new BigDecimal("5000")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // Updating the credit limit for a valid account should apply the new limit and save the new value
    @Test
    void updateCreditLimit_success_setsCreditLimitAndSaves() {
        BigDecimal newLimit = new BigDecimal("10000.00");
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(merchantAccount));
        when(userAccountRepository.save(merchantAccount)).thenReturn(merchantAccount);

        UserAccount result = userService.updateCreditLimit(1L, newLimit);

        assertThat(result.getCreditLimit()).isEqualByComparingTo(newLimit);
        verify(userAccountRepository).save(merchantAccount);
    }


    //   updateAllMerchantStatuses
    // A merchant more than 30 days past their payment due date should be moved to IN_DEFAULT
    @Test
    void updateAllMerchantStatuses_overdueMoreThan30Days_setsInDefault() {
        merchantAccount.setBalance(new BigDecimal("100.00"));
        merchantAccount.setPaymentDueDate(LocalDate.now().minusDays(31));

        when(userAccountRepository.findByAccountType(UserAccount.AccountType.MERCHANT))
                .thenReturn(List.of(merchantAccount));

        userService.updateAllMerchantStatuses();

        assertThat(merchantAccount.getAccountStatus()).isEqualTo(UserAccount.AccountStatus.IN_DEFAULT);
        verify(userAccountRepository).save(merchantAccount);
    }

    // A merchant between 15 and 30 days overdue should be SUSPENDED (but can still be restored by paying)
    @Test
    void updateAllMerchantStatuses_overdueMoreThan15Days_setsSuspended() {
        merchantAccount.setBalance(new BigDecimal("100.00"));
        merchantAccount.setPaymentDueDate(LocalDate.now().minusDays(16));

        when(userAccountRepository.findByAccountType(UserAccount.AccountType.MERCHANT))
                .thenReturn(List.of(merchantAccount));

        userService.updateAllMerchantStatuses();

        assertThat(merchantAccount.getAccountStatus()).isEqualTo(UserAccount.AccountStatus.SUSPENDED);
        verify(userAccountRepository).save(merchantAccount);
    }

    // A merchant who is not overdue should remain on NORMAL status
    @Test
    void updateAllMerchantStatuses_notOverdue_setsNormal() {
        merchantAccount.setPaymentDueDate(LocalDate.now().plusDays(5));

        when(userAccountRepository.findByAccountType(UserAccount.AccountType.MERCHANT))
                .thenReturn(List.of(merchantAccount));

        userService.updateAllMerchantStatuses();

        assertThat(merchantAccount.getAccountStatus()).isEqualTo(UserAccount.AccountStatus.NORMAL);
    }

    // A merchant with no payment due date set should be skipped entirely & so no status change
    @Test
    void updateAllMerchantStatuses_nullPaymentDueDate_skipsAccount() {
        merchantAccount.setPaymentDueDate(null);
        merchantAccount.setAccountStatus(UserAccount.AccountStatus.NORMAL);

        when(userAccountRepository.findByAccountType(UserAccount.AccountType.MERCHANT))
                .thenReturn(List.of(merchantAccount));

        userService.updateAllMerchantStatuses();

        assertThat(merchantAccount.getAccountStatus()).isEqualTo(UserAccount.AccountStatus.NORMAL);
        verify(userAccountRepository, never()).save(any());
    }

    // Exactly 30 days overdue is still SUSPENDED (the threshold for IN_DEFAULT is strictly greater than 30)
    @Test
    void updateAllMerchantStatuses_exactly30DaysOverdue_setsSuspended() {
        merchantAccount.setBalance(new BigDecimal("100.00"));
        merchantAccount.setPaymentDueDate(LocalDate.now().minusDays(30));

        when(userAccountRepository.findByAccountType(UserAccount.AccountType.MERCHANT))
                .thenReturn(List.of(merchantAccount));

        userService.updateAllMerchantStatuses();

        assertThat(merchantAccount.getAccountStatus()).isEqualTo(UserAccount.AccountStatus.SUSPENDED);
    }
}