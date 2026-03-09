package com.infopharma.ipos_sa.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "order_id", length = 10)
    private String orderId; // e.g., "IP2034"

    @Column(name = "account_id", length = 10, nullable = false)
    private String accountId; // FK to UserAccount

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "total_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "dispatched_by", length = 100)
    private String dispatchedBy;

    @Column(name = "dispatch_date")
    private LocalDate dispatchDate;

    @Column(length = 100)
    private String courier;

    @Column(name = "courier_ref", length = 100)
    private String courierRef;

    @Column(name = "expected_delivery")
    private LocalDate expectedDelivery;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "discount_applied", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountApplied = BigDecimal.valueOf(0.00);

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    public enum OrderStatus {
        ACCEPTED,
        BEING_PROCESSED,
        DISPATCHED,
        DELIVERED
    }

    public enum PaymentStatus {
        PENDING,
        PAID
    }
}