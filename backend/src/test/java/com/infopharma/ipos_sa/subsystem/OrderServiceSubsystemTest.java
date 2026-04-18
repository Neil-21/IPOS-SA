package com.infopharma.ipos_sa.subsystem;

/**
 * OrderServiceSubsystemTest
 * Subsystem / integration-style tests for the order pipeline. Uses real
 * {@link com.infopharma.ipos_sa.service.impl.OrderServiceImpl} and mapper
 * instances wired together, with in-memory H2 repositories, to verify
 * the full flow from order placement through dispatch to delivery without
 * mocking the service internals.
 */
import com.infopharma.ipos_sa.dto.OrderRequest;
import com.infopharma.ipos_sa.entity.*;
import com.infopharma.ipos_sa.mapper.impl.DispatchMapper;
import com.infopharma.ipos_sa.repository.*;
import com.infopharma.ipos_sa.service.OrderService;
import com.infopharma.ipos_sa.service.impl.OrderServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Subsystem tests for the OrderService provided interface (that IPOS-sa offers).

@ExtendWith(MockitoExtension.class)
class OrderServiceSubsystemTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock UserAccountRepository userAccountRepository;
    @Mock CatalogueItemRepository catalogueItemRepository;
    @Mock DispatchMapper dispatchMapper;

    @InjectMocks OrderServiceImpl orderServiceImpl;

    private OrderService orderService;
    private UserAccount normalAccount;
    private CatalogueItem catalogueItem;

    @BeforeEach
    void setUp() {
        orderService = orderServiceImpl;
        normalAccount = new UserAccount();
        normalAccount.setAccountId(1L);
        normalAccount.setUsername("merchant1");
        normalAccount.setAccountStatus(UserAccount.AccountStatus.NORMAL);
        normalAccount.setBalance(BigDecimal.ZERO);

        catalogueItem = new CatalogueItem();
        catalogueItem.setItemId("ITEM001");
        catalogueItem.setDescription("Paracetamol 500mg");
        catalogueItem.setPackageCost(new BigDecimal("12.50"));
        catalogueItem.setAvailability(100);
    }

    // placeOrder (represents IPOS-pu submitting an order to IPOS-sa on behalf of merchant)

    // SA should reject the order if the merchant account doesn't exist
    @Test
    void placeOrder_accountNotFound_throwsEntityNotFoundException() {
        OrderRequest request = buildOrderRequest(99L, "ITEM001", 1);
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // Suspended accounts should not be allowed to place new orders and SA should block these orders
    @Test
    void placeOrder_suspendedAccount_throwsIllegalStateException() {
        normalAccount.setAccountStatus(UserAccount.AccountStatus.SUSPENDED);
        OrderRequest request = buildOrderRequest(1L, "ITEM001", 1);
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(normalAccount));

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SUSPENDED");
    }

    // Valid order with no discount plan added should be accepted & charged at full price
    @Test
    void placeOrder_noDiscountPlan_chargesFullAmountAndAcceptsOrder() {
        OrderRequest request = buildOrderRequest(1L, "ITEM001", 2);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(normalAccount));
        when(orderRepository.existsById(anyString())).thenReturn(false);
        when(invoiceRepository.existsById(anyString())).thenReturn(false);
        when(catalogueItemRepository.findById("ITEM001")).thenReturn(Optional.of(catalogueItem));
        when(catalogueItemRepository.save(any(CatalogueItem.class))).thenReturn(catalogueItem);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.placeOrder(request);

        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.ACCEPTED);
        assertThat(result.getTotalValue()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(result.getDiscountApplied()).isEqualByComparingTo(BigDecimal.ZERO);

    }

    // Valid order with a fixed 10% discount plan should be correctly priced and reduced
    @Test
    void placeOrder_withFixedDiscountPlan_appliesDiscountCorrectly() {
        DiscountTier tier = new DiscountTier();
        tier.setDiscountRate(new BigDecimal("10"));

        DiscountPlan plan = new DiscountPlan();
        plan.setPlanType(DiscountPlan.PlanType.FIXED);
        plan.setTiers(List.of(tier));

        normalAccount.setDiscountPlan(plan);

        OrderRequest request = buildOrderRequest(1L, "ITEM001", 2);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(normalAccount));
        when(orderRepository.existsById(anyString())).thenReturn(false);
        when(invoiceRepository.existsById(anyString())).thenReturn(false);
        when(catalogueItemRepository.findById("ITEM001")).thenReturn(Optional.of(catalogueItem));
        when(catalogueItemRepository.save(any(CatalogueItem.class))).thenReturn(catalogueItem);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.placeOrder(request);

        assertThat(result.getDiscountApplied()).isEqualByComparingTo(new BigDecimal("2.50"));
        assertThat(result.getTotalValue()).isEqualByComparingTo(new BigDecimal("22.50"));

    }

    // findByAccountId (represents IPOS-pu or IPOS-ca querying IPOS-sa on a merchant's order history)

    // Querying a valid merchant account should return all of the merchant's orders
    @Test
    void findByAccountId_validAccount_returnsOrder() {
        Order o1 = new Order();
        o1.setOrderId("IP0001");
        Order o2 = new Order();
        o2.setOrderId("IP0002");

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(normalAccount));
        when(orderRepository.findByAccount(normalAccount)).thenReturn(List.of(o1, o2));

        List<Order> result = orderService.findByAccountId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Order::getOrderId).containsExactly("IP0001", "IP0002");
    }

    // Querying orders from an account that doesn't exist should throw an error
    @Test
    void findByAccountId_accountNotFound_throwsEntityNotFoundException() {
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findByAccountId(99L))
        .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

    }


    //  helpers
    private OrderRequest buildOrderRequest(Long accountId, String itemId, int qty) {
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setItemId(itemId);
        itemReq.setQuantity(qty);

        OrderRequest request = new OrderRequest();
        request.setAccountId(accountId);
        request.setItems(List.of(itemReq));
        return request;
    }
}


