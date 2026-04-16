package com.infopharma.ipos_sa.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infopharma.ipos_sa.controller.UserAccountController;
import com.infopharma.ipos_sa.dto.*;
import com.infopharma.ipos_sa.entity.UserAccount;
import com.infopharma.ipos_sa.mapper.impl.UpdateAccountDetailsMapper;
import com.infopharma.ipos_sa.mapper.impl.UpdateMerchantMapper;
import com.infopharma.ipos_sa.mapper.impl.UserAccountMapper;
import com.infopharma.ipos_sa.service.ReportService;
import com.infopharma.ipos_sa.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * System tests for the UserAccount REST API endpoints.
 *
 * These tests verify the full HTTP request-response cycle for the
 * /api/accounts endpoints that other systems (IPOS-PU, IPOS-CA) use to
 * manage merchant accounts. The service and mapper layers are mocked so
 * the tests run without a real database.
 *
 * The GlobalExceptionHandler is active, so EntityNotFoundException thrown
 * by the service layer correctly maps to 404 responses.
 *
 * Test IDs match the System Testing plan document (ST17 - ST28).
 */
@WebMvcTest(UserAccountController.class)
class UserAccountSystemTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Service mocks
    @MockitoBean UserService userService;
    @MockitoBean ReportService reportService;

    // Mapper mocks — each implements a different Mapper<A, B> type parameter,
    // so Spring can distinguish and inject them into the controller constructor
    @MockitoBean UserAccountMapper userAccountMapper;
    @MockitoBean UpdateMerchantMapper updateMerchantMapper;
    @MockitoBean UpdateAccountDetailsMapper updateAccountDetailsMapper;
    @MockitoBean ModelMapper modelMapper;

    // ─────────────────────────────────────────────────────────
    //  POST /api/accounts  —  create a new account
    // ─────────────────────────────────────────────────────────

    /**
     * ST17 — A valid account creation request should return 201 Created
     * with the saved account in the response body.
     */
    @Test
    void ST17_createAccount_validRequest_returns201WithAccount() throws Exception {
        CreateUserAccountRequest request = new CreateUserAccountRequest();
        request.setUsername("merchant1");
        request.setPassword("secret");
        request.setAccountType(UserAccount.AccountType.MERCHANT);
        request.setAccountStatus(UserAccount.AccountStatus.NORMAL);
        request.setPhone("01234567890");
        request.setEmail("merchant@test.com");

        UserAccount saved = buildAccount(1L, "merchant1", UserAccount.AccountStatus.NORMAL);

        // The mapper converts the DTO into a UserAccount entity
        when(userAccountMapper.mapFrom(any(CreateUserAccountRequest.class))).thenReturn(saved);
        when(userService.createAccount(any(UserAccount.class))).thenReturn(saved);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.username").value("merchant1"));
    }

    // ─────────────────────────────────────────────────────────
    //  GET /api/accounts  —  list all accounts
    // ─────────────────────────────────────────────────────────

    /**
     * ST18 — Fetching all accounts should return 200 OK with a JSON array
     * containing every account in the system.
     */
    @Test
    void ST18_getAllAccounts_returns200WithList() throws Exception {
        UserAccount a1 = buildAccount(1L, "merchant1", UserAccount.AccountStatus.NORMAL);
        UserAccount a2 = buildAccount(2L, "merchant2", UserAccount.AccountStatus.SUSPENDED);

        when(userService.findAll()).thenReturn(List.of(a1, a2));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("merchant1"))
                .andExpect(jsonPath("$[1].username").value("merchant2"));
    }

    // ─────────────────────────────────────────────────────────
    //  GET /api/accounts/{id}  —  fetch a single account
    // ─────────────────────────────────────────────────────────

    /**
     * ST19 — Fetching an account that exists should return 200 OK with
     * the full account details in the response body.
     */
    @Test
    void ST19_getAccount_existingId_returns200WithAccount() throws Exception {
        UserAccount account = buildAccount(1L, "merchant1", UserAccount.AccountStatus.NORMAL);

        when(userService.findOne(1L)).thenReturn(Optional.of(account));

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.username").value("merchant1"))
                .andExpect(jsonPath("$.accountStatus").value("NORMAL"));
    }

    /**
     * ST20 — Fetching an account ID that does not exist should return
     * 404 Not Found (controller returns notFound() for an empty Optional).
     */
    @Test
    void ST20_getAccount_nonExistingId_returns404() throws Exception {
        when(userService.findOne(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/accounts/99"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────
    //  GET /api/accounts/{id}/balance  —  get current balance
    // ─────────────────────────────────────────────────────────

    /**
     * ST21 — Fetching the balance of an existing account should return
     * 200 OK with the accountId and current balance in the response.
     */
    @Test
    void ST21_getBalance_existingAccount_returns200WithBalance() throws Exception {
        UserAccount account = buildAccount(1L, "merchant1", UserAccount.AccountStatus.NORMAL);
        account.setBalance(new BigDecimal("150.00"));

        when(userService.findOne(1L)).thenReturn(Optional.of(account));

        mockMvc.perform(get("/api/accounts/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.balance").value(150.00));
    }

    /**
     * ST22 — Requesting the balance for an account that does not exist
     * should return 404 Not Found.
     */
    @Test
    void ST22_getBalance_nonExistingAccount_returns404() throws Exception {
        when(userService.findOne(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/accounts/99/balance"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────
    //  DELETE /api/accounts/{id}  —  delete account (with cascade)
    // ─────────────────────────────────────────────────────────

    /**
     * ST23 — Deleting an existing account should return 204 No Content.
     * The service is responsible for removing related invoices and orders first.
     */
    @Test
    void ST23_deleteAccount_existingId_returns204() throws Exception {
        UserAccount account = buildAccount(1L, "merchant1", UserAccount.AccountStatus.NORMAL);

        when(userService.findOne(1L)).thenReturn(Optional.of(account));
        doNothing().when(userService).deleteAccount(1L);

        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteAccount(1L);
    }

    /**
     * ST24 — Attempting to delete an account ID that does not exist should
     * return 404 Not Found (controller checks findOne before deleting).
     */
    @Test
    void ST24_deleteAccount_nonExistingId_returns404() throws Exception {
        when(userService.findOne(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/accounts/99"))
                .andExpect(status().isNotFound());

        verify(userService, never()).deleteAccount(anyLong());
    }

    // ─────────────────────────────────────────────────────────
    //  PUT /api/accounts/{id}/credit-limit  —  update credit limit
    // ─────────────────────────────────────────────────────────

    /**
     * ST25 — Updating the credit limit for a valid account should return
     * 200 OK with the accountId and the new credit limit confirmed in the body.
     */
    @Test
    void ST25_updateCreditLimit_validAccount_returns200WithNewLimit() throws Exception {
        UpdateCreditLimitRequest request = new UpdateCreditLimitRequest();
        request.setCreditLimit(new BigDecimal("5000.00"));

        UserAccount updated = buildAccount(1L, "merchant1", UserAccount.AccountStatus.NORMAL);
        updated.setCreditLimit(new BigDecimal("5000.00"));

        when(userService.updateCreditLimit(1L, new BigDecimal("5000.00"))).thenReturn(updated);

        mockMvc.perform(put("/api/accounts/1/credit-limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.creditLimit").value(5000.00));
    }

    /**
     * ST26 — Attempting to update the credit limit for an account that does
     * not exist should return 404 Not Found
     * (EntityNotFoundException → GlobalExceptionHandler).
     */
    @Test
    void ST26_updateCreditLimit_accountNotFound_returns404() throws Exception {
        UpdateCreditLimitRequest request = new UpdateCreditLimitRequest();
        request.setCreditLimit(new BigDecimal("5000.00"));

        when(userService.updateCreditLimit(eq(99L), any())).thenThrow(
                new EntityNotFoundException("Account not found: 99"));

        mockMvc.perform(put("/api/accounts/99/credit-limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────
    //  PUT /api/accounts/{id}/discount-plan  —  assign discount plan
    // ─────────────────────────────────────────────────────────

    /**
     * ST27 — Assigning a valid discount plan to an existing account should
     * return 204 No Content (plan applied, nothing to return in the body).
     */
    @Test
    void ST27_assignDiscountPlan_validAccountAndPlan_returns204() throws Exception {
        UpdateDiscountPlanAssignRequest request = new UpdateDiscountPlanAssignRequest();
        request.setDiscountPlanId(4);

        UserAccount account = buildAccount(1L, "merchant1", UserAccount.AccountStatus.NORMAL);
        when(userService.updateDiscountPlan(1L, 4)).thenReturn(account);

        mockMvc.perform(put("/api/accounts/1/discount-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    /**
     * ST28 — Assigning a discount plan where either the account or the plan
     * does not exist should return 404 Not Found
     * (EntityNotFoundException → GlobalExceptionHandler).
     */
    @Test
    void ST28_assignDiscountPlan_notFound_returns404() throws Exception {
        UpdateDiscountPlanAssignRequest request = new UpdateDiscountPlanAssignRequest();
        request.setDiscountPlanId(99);

        when(userService.updateDiscountPlan(eq(1L), eq(99))).thenThrow(
                new EntityNotFoundException("Discount plan not found: 99"));

        mockMvc.perform(put("/api/accounts/1/discount-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────

    private UserAccount buildAccount(Long id, String username, UserAccount.AccountStatus status) {
        UserAccount account = new UserAccount();
        account.setAccountId(id);
        account.setUsername(username);
        account.setAccountStatus(status);
        account.setAccountType(UserAccount.AccountType.MERCHANT);
        account.setBalance(BigDecimal.ZERO);
        account.setPassword("secret");
        account.setPhone("01234567890");
        account.setEmail("test@test.com");
        account.setIsActive(true);
        return account;
    }
}
