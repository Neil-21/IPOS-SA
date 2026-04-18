package com.infopharma.ipos_sa.dto;

/**
 * DetailedOrderReport
 * DTO returned by {@code GET /api/reports/merchant-detailed}. Wraps a list
 * of {@code DetailedOrderRow} objects, each containing order metadata and a
 * nested list of {@code ItemRow} line items for one order in the requested
 * date range.
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class DetailedOrderReport {
    private Long accountId;
    private String companyName;
    private LocalDate from;
    private LocalDate to;
    private List<DetailedOrderRow> orders;
    private BigDecimal grandTotal;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailedOrderRow {
        private String orderId;
        private LocalDate orderDate;
        private List<ItemRow> items;
        private BigDecimal totalValue;     // matches Order.totalValue — enables auto-mapping
        private BigDecimal discountApplied;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ItemRow {
            private String itemId;
            private String description;
            private int quantity;
            private BigDecimal unitCost;
            private BigDecimal totalCost;
        }
    }
}
