package com.infopharma.ipos_sa.dto;

/**
 * LowStockReportItem
 * DTO for a single row in the low-stock report. Contains the catalogue item
 * details, current availability, stock limit, and the recommended minimum
 * re-order quantity.
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LowStockReportItem {
    private String itemId;
    private String description;
    private int currentAvailability;
    private int minStockLevel;
    private int recommendedOrderQty;
}
