package com.infopharma.ipos_sa.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infopharma.ipos_sa.controller.OrderController;
import com.infopharma.ipos_sa.dto.DispatchRequest;
import com.infopharma.ipos_sa.dto.OrderRequest;
import com.infopharma.ipos_sa.entity.Order;
import com.infopharma.ipos_sa.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * System tests for the Order REST API endpoints.
 *
 * These tests verify that the HTTP layer behaves correctly end-to-end:
 * the right status codes are returned, the JSON response contains the
 * expected fields, and error responses are formatted correctly.
 *
 * The service layer is mocked so tests run without a real database.
 * The GlobalExceptionHandler is active, so exceptions thrown by the
 * service are mapped to the expected HTTP status codes (404 / 400).
 *
 * Test IDs match the System Testing plan document (ST01 - ST16).
 */
@WebMvcTest(OrderController.class)
class OrderSystemTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean OrderService orderService;

    // ─────────────────────────────────────────────────────────
    //  POST /api/orders  —  place a new order
    // ─────────────────────────────────────────────────────────

    /**
     * ST01 — A well-formed order request from a valid merchant should be
     * accepted and return 201 Created with the order details in the body.
     */
    @Test
    void ST01_placeOrder_validRequest_returns201WithOrder() throws Exception {
        OrderRequest request = buildOrderRequest(1L, "ITEM001", 2);
        Order created = buildOrder("IP1001", Order.OrderStatus.ACCEPTED, Order.PaymentStatus.PENDING, "25.00");

        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("IP1001"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.totalValue").value(25.00));
    }

    /**
     * ST02 — If the merchant account ID does not exist, the service throws
     * EntityNotFoundException, which should map to 404 Not Found.
     */
    @Test
    void ST02_placeOrder_accountNotFound_returns404() throws Exception {
        OrderRequest request = buildOrderRequest(99L, "ITEM001", 1);

        when(orderService.placeOrder(any())).thenThrow(
                new EntityNotFoundException("Account not found: 99"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    /**
     * ST03 — A suspended merchant is not allowed to place orders.
     * The service throws IllegalStateException, which maps to 400 Bad Request.
     */
    @Test
    void ST03_placeOrder_suspendedAccount_returns400() throws Exception {
        OrderRequest request = buildOrderRequest(1L, "ITEM001", 1);

        when(orderService.placeOrder(any())).thenThrow(
                new IllegalStateException("Account is SUSPENDED"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SUSPENDED")));
    }

    /**
     * ST04 — An account marked IN_DEFAULT cannot place orders.
     * The service throws IllegalStateException, which maps to 400 Bad Request.
     */
    @Test
    void ST04_placeOrder_inDefaultAccount_returns400() throws Exception {
        OrderRequest request = buildOrderRequest(1L, "ITEM001", 1);

        when(orderService.placeOrder(any())).thenThrow(
                new IllegalStateException("Account is IN_DEFAULT"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("IN_DEFAULT")));
    }

    /**
     * ST05 — If the requested catalogue item does not exist, the service
     * throws EntityNotFoundException, which maps to 404 Not Found.
     */
    @Test
    void ST05_placeOrder_itemNotFound_returns404() throws Exception {
        OrderRequest request = buildOrderRequest(1L, "MISSING_ITEM", 1);

        when(orderService.placeOrder(any())).thenThrow(
                new EntityNotFoundException("Item not found: MISSING_ITEM"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────
    //  GET /api/orders  —  list all orders
    // ─────────────────────────────────────────────────────────

    /**
     * ST06 — Fetching all orders should return 200 OK with a JSON array
     * containing every order in the system.
     */
    @Test
    void ST06_getAllOrders_returns200WithAllOrders() throws Exception {
        Order o1 = buildOrder("IP0001", Order.OrderStatus.ACCEPTED, Order.PaymentStatus.PENDING, "100.00");
        Order o2 = buildOrder("IP0002", Order.OrderStatus.DELIVERED, Order.PaymentStatus.PAID, "200.00");

        when(orderService.findAll()).thenReturn(List.of(o1, o2));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value("IP0001"))
                .andExpect(jsonPath("$[1].orderId").value("IP0002"));
    }

    /**
     * ST07 — When there are no orders yet the response should still be
     * 200 OK with an empty JSON array (not a 404 or error).
     */
    @Test
    void ST07_getAllOrders_noOrders_returns200WithEmptyArray() throws Exception {
        when(orderService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─────────────────────────────────────────────────────────
    //  GET /api/orders/incomplete  —  orders not yet delivered
    // ─────────────────────────────────────────────────────────

    /**
     * ST08 — The incomplete-orders endpoint should return 200 OK with only
     * orders that have not yet reached DELIVERED status.
     */
    @Test
    void ST08_getIncompleteOrders_returns200WithNonDeliveredOrders() throws Exception {
        Order dispatched = buildOrder("IP0003", Order.OrderStatus.DISPATCHED, Order.PaymentStatus.PENDING, "75.00");

        when(orderService.findIncomplete()).thenReturn(List.of(dispatched));

        mockMvc.perform(get("/api/orders/incomplete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("DISPATCHED"));
    }

    // ─────────────────────────────────────────────────────────
    //  GET /api/orders/my  —  orders for a specific merchant
    // ─────────────────────────────────────────────────────────

    /**
     * ST09 — A merchant querying their own orders should receive 200 OK
     * with the list of orders belonging to that account.
     */
    @Test
    void ST09_getMyOrders_validAccountId_returns200WithOrders() throws Exception {
        Order order = buildOrder("IP0004", Order.OrderStatus.ACCEPTED, Order.PaymentStatus.PENDING, "50.00");

        when(orderService.findByAccountId(1L)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/my").param("accountId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value("IP0004"));
    }

    /**
     * ST10 — Querying orders for an account ID that does not exist should
     * return 404 Not Found (EntityNotFoundException → GlobalExceptionHandler).
     */
    @Test
    void ST10_getMyOrders_accountNotFound_returns404() throws Exception {
        when(orderService.findByAccountId(99L)).thenThrow(
                new EntityNotFoundException("Account not found: 99"));

        mockMvc.perform(get("/api/orders/my").param("accountId", "99"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────
    //  GET /api/orders/{id}  —  fetch a single order by ID
    // ─────────────────────────────────────────────────────────

    /**
     * ST11 — Fetching an order that exists should return 200 OK with the
     * full order details in the response body.
     */
    @Test
    void ST11_getOrder_existingId_returns200WithOrder() throws Exception {
        Order order = buildOrder("IP0005", Order.OrderStatus.ACCEPTED, Order.PaymentStatus.PENDING, "120.00");

        when(orderService.findById("IP0005")).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/IP0005"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("IP0005"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    /**
     * ST12 — Fetching an order ID that does not exist should return
     * 404 Not Found (the controller returns notFound() for an empty Optional).
     */
    @Test
    void ST12_getOrder_nonExistingId_returns404() throws Exception {
        when(orderService.findById("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────
    //  PUT /api/orders/{id}/dispatch  —  mark order as dispatched
    // ─────────────────────────────────────────────────────────

    /**
     * ST13 — Dispatching a valid order should return 200 OK with the order
     * status set to DISPATCHED and the courier details present in the body.
     */
    @Test
    void ST13_dispatchOrder_validRequest_returns200WithDispatchedOrder() throws Exception {
        DispatchRequest dispatchRequest = new DispatchRequest();
        dispatchRequest.setDispatchedBy("Alice");
        dispatchRequest.setCourier("DHL");
        dispatchRequest.setCourierRef("DHL-999");
        dispatchRequest.setExpectedDelivery(LocalDate.now().plusDays(3));

        Order dispatched = buildOrder("IP0006", Order.OrderStatus.DISPATCHED, Order.PaymentStatus.PENDING, "90.00");
        dispatched.setDispatchedBy("Alice");
        dispatched.setCourier("DHL");
        dispatched.setCourierRef("DHL-999");
        dispatched.setDispatchDate(LocalDate.now());

        when(orderService.dispatch(eq("IP0006"), any(DispatchRequest.class))).thenReturn(dispatched);

        mockMvc.perform(put("/api/orders/IP0006/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dispatchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPATCHED"))
                .andExpect(jsonPath("$.dispatchedBy").value("Alice"))
                .andExpect(jsonPath("$.courier").value("DHL"));
    }

    /**
     * ST14 — Attempting to dispatch an order that does not exist should
     * return 404 Not Found (EntityNotFoundException → GlobalExceptionHandler).
     */
    @Test
    void ST14_dispatchOrder_orderNotFound_returns404() throws Exception {
        when(orderService.dispatch(eq("UNKNOWN"), any())).thenThrow(
                new EntityNotFoundException("Order not found: UNKNOWN"));

        mockMvc.perform(put("/api/orders/UNKNOWN/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DispatchRequest())))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────
    //  PUT /api/orders/{id}/delivered  —  mark order as delivered
    // ─────────────────────────────────────────────────────────

    /**
     * ST15 — Marking a valid dispatched order as delivered should return
     * 200 OK with status DELIVERED and today's delivery date.
     */
    @Test
    void ST15_markDelivered_validId_returns200WithDeliveredOrder() throws Exception {
        Order delivered = buildOrder("IP0007", Order.OrderStatus.DELIVERED, Order.PaymentStatus.PENDING, "60.00");
        delivered.setDeliveryDate(LocalDate.now());

        when(orderService.markDelivered("IP0007")).thenReturn(delivered);

        mockMvc.perform(put("/api/orders/IP0007/delivered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    /**
     * ST16 — Attempting to mark a non-existent order as delivered should
     * return 404 Not Found (EntityNotFoundException → GlobalExceptionHandler).
     */
    @Test
    void ST16_markDelivered_orderNotFound_returns404() throws Exception {
        when(orderService.markDelivered("UNKNOWN")).thenThrow(
                new EntityNotFoundException("Order not found: UNKNOWN"));

        mockMvc.perform(put("/api/orders/UNKNOWN/delivered"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────

    private Order buildOrder(String id, Order.OrderStatus status,
                             Order.PaymentStatus paymentStatus, String total) {
        Order order = new Order();
        order.setOrderId(id);
        order.setStatus(status);
        order.setPaymentStatus(paymentStatus);
        order.setTotalValue(new BigDecimal(total));
        order.setOrderDate(LocalDate.now());
        order.setDiscountApplied(BigDecimal.ZERO);
        return order;
    }

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
