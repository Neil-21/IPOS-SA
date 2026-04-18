package com.infopharma.ipos_sa.entity;

/**
 * UserAccount
 * JPA entity representing any system account — merchant (WHOLESALER /
 * RETAILER) or internal staff (ADMIN / MANAGER / CLERK / WAREHOUSE /
 * DELIVERY). Holds login credentials, company details, credit limit,
 * account status, and a reference to the assigned discount plan.
 *
 * Enums:
 *   AccountType   — WHOLESALER | RETAILER | ADMIN | MANAGER | CLERK |
 *                   WAREHOUSE | DELIVERY
 *   AccountStatus — NORMAL | SUSPENDED | IN_DEFAULT
 */
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_accounts")
@ToString(exclude = {"discountPlan", "monthlyDiscounts"})
@EqualsAndHashCode(of = "accountId")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(length = 50, nullable = false)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(length = 255)
    private String address;

    @Column(length = 20, nullable = false)
    private String phone;

    @Column(length = 20)
    private String fax;

    @Column(length = 100, nullable = false)
    private String email;

    @Column(name = "credit_limit", precision = 10, scale = 2)
    private BigDecimal creditLimit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "discount_plan_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DiscountPlan discountPlan;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;

    @Column(name = "role", length = 50)
    private String role;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MonthlyDiscount> monthlyDiscounts;

    public enum AccountType  { MERCHANT, ADMIN, MANAGER }
    public enum AccountStatus { NORMAL, SUSPENDED, IN_DEFAULT }
}
