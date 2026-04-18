package com.infopharma.ipos_sa.entity;

/**
 * Order
 * JPA entity representing a merchant purchase order. Tracks the full
 * lifecycle from submission through to delivery, along with payment status.
 *
 * Enums:
 *   OrderStatus   — SUBMITTED | ACCEPTED | READY_TO_DISPATCH | DISPATCHED |
 *                   DELIVERED | CANCELLED
 *   PaymentStatus — PENDING | RECEIVED | OVERDUE
 *
 * When an order reaches DELIVERED status the service layer generates an
 * {@link Invoice} and deducts availability from affected {@link CatalogueItem}s.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
@ToString(exclude = "account")
@EqualsAndHashCode(of = "orderId")
public class Order {

    @Id
    @Column(name = "order_id", length = 10)
    private String orderId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnoreProperties({"discountPlan", "monthlyDiscounts", "password",
                           "hibernateLazyInitializer", "handler"})
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserAccount account;

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
    @Builder.Default
    private BigDecimal discountApplied = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"order", "hibernateLazyInitializer", "handler"})
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public enum OrderStatus {
        ACCEPTED, BEING_PROCESSED, DISPATCHED, DELIVERED
    }

    public enum PaymentStatus {
        PENDING, PAID
    }
}
