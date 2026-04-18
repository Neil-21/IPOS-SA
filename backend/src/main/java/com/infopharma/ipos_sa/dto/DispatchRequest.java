package com.infopharma.ipos_sa.dto;

/**
 * DispatchRequest
 * DTO carrying courier and dispatch details submitted when an order is
 * marked as dispatched. Fields: courier name, tracking reference, dispatch
 * date, and expected delivery date.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DispatchRequest {
    private String dispatchedBy;
    private String courier;
    private String courierRef;
    private LocalDate expectedDelivery;
}
