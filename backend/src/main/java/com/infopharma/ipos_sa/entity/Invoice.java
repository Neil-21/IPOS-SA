package com.infopharma.ipos_sa.entity;

/**
 * Invoice
 * JPA entity representing the invoice generated when an order is delivered.
 * Stores subtotal, discount applied, total amount, due date, and payment status.
 * Linked many-to-one to {@link UserAccount} (the merchant) and one-to-one
 * to the fulfilling {@link Order}.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invoices")
@ToString(exclude = {"order", "account"})
@EqualsAndHashCode(of = "invoiceId")
public class Invoice {

    @Id
    @Column(name = "invoice_id", length = 10)
    private String invoiceId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"account", "hibernateLazyInitializer", "handler"})
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnoreProperties({"discountPlan", "monthlyDiscounts", "password",
                           "hibernateLazyInitializer", "handler"})
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserAccount account;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "amount_due", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountDue;
}
