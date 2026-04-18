package com.infopharma.ipos_sa.dto;

/**
 * StockAddRequest
 * DTO for restocking a catalogue item. Carries the quantity to add and the
 * username of the staff member recording the delivery. Used by
 * {@code POST /api/catalogue/{id}/restock}.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockAddRequest {
    private Integer quantity;
    private String recordedBy;
}
