package com.infopharma.ipos_sa.dto;

/**
 * OrderRequest
 * DTO submitted when placing a new order. Carries the merchant account ID
 * and a list of {@code OrderItemRequest} objects (catalogue item ID and
 * quantity). The service layer resolves prices and calculates totals.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {

    private Long accountId;
    private List<OrderItemRequest> items;

    @Data
    @NoArgsConstructor
    public static class OrderItemRequest {
        private String itemId;
        private Integer quantity;
    }
}
