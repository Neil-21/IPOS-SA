package com.infopharma.ipos_sa.entity;

/**
 * DiscountPlan
 * JPA entity representing a merchant discount plan. Plans can be either
 * FIXED (a single flat discount rate) or FLEXIBLE (tiered rates based on
 * cumulative monthly order value). Holds a one-to-many list of
 * {@link DiscountTier}s for flexible plans.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "discount_plans")
@ToString(exclude = "tiers")
@EqualsAndHashCode(of = "discountPlanId")
public class DiscountPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_plan_id")
    private Integer discountPlanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    @OneToMany(mappedBy = "discountPlan", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"discountPlan", "hibernateLazyInitializer", "handler"})
    @Builder.Default
    private List<DiscountTier> tiers = new ArrayList<>();

    public enum PlanType { FIXED, FLEXIBLE }
}
