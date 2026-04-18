package com.infopharma.ipos_sa.entity;

/**
 * StockDelivery
 * JPA entity recording an inbound stock delivery for a catalogue item.
 * Used by the stock-turnover report to calculate opening and closing stock
 * levels. Stores quantity received, delivery date, and the staff member
 * who recorded the receipt.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock_deliveries")
@ToString(exclude = "item")
@EqualsAndHashCode(of = "deliveryId")
public class StockDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Integer deliveryId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CatalogueItem item;

    @Column(name = "quantity_received", nullable = false)
    private Integer quantityReceived;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "recorded_by", length = 100)
    private String recordedBy;
}
