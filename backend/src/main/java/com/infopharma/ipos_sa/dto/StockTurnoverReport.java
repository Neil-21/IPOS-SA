package com.infopharma.ipos_sa.dto;

/**
 * StockTurnoverReport
 * DTO returned by {@code GET /api/reports/stock-turnover}. Contains the
 * report date range and a list of {@code StockTurnoverRow} objects showing
 * opening stock, units sold, units received, and closing stock per item.
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class StockTurnoverReport {
    private LocalDate from;
    private LocalDate to;
    private List<StockTurnoverRow> rows;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StockTurnoverRow {
        private String itemId;
        private String description;
        private int quantityDelivered;
        private int quantitySold;
        private int netChange;
    }
}
