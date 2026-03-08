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
@Table(name = "monthly_discounts")
public class MonthlyDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_discount_id")
    private Integer monthlyDiscountId;

    @Column(name = "account_id", length = 10, nullable = false)
    private String accountId; // FK to UserAccount, stored as string for now

    @Column(name = "month_year", nullable = false)
    private LocalDate monthYear; // end of the calendar month

    @Column(name = "total_orders_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalOrdersValue; // sum of orders in the month

    @Column(name = "discount_rate_applied", precision = 5, scale = 2, nullable = false)
    private BigDecimal discountRateApplied; // the tier rate applied

    @Column(name = "discount_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountAmount; // calculated discount value

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_method", nullable = false)
    private SettlementMethod settlementMethod; // cheque or order_deduction

    @Column(nullable = false)
    private Boolean settled = false; // whether paid/deducted

    public enum SettlementMethod {
        CHEQUE, ORDER_DEDUCTION
    }
}