package com.infopharma.ipos_sa.mapper.impl;

/**
 * OrderSummaryRowMapper
 * Maps an {@link com.infopharma.ipos_sa.entity.Order} to a
 * {@link com.infopharma.ipos_sa.dto.MerchantOrdersSummary.OrderSummaryRow}.
 * Custom TypeMap converts the {@code PaymentStatus} enum to its string name
 * so the DTO stays serialisation-agnostic.
 */
import com.infopharma.ipos_sa.dto.MerchantOrdersSummary;
import com.infopharma.ipos_sa.entity.Order;
import com.infopharma.ipos_sa.mapper.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class OrderSummaryRowMapper implements Mapper<Order, MerchantOrdersSummary.OrderSummaryRow> {

    private final ModelMapper modelMapper;

    public OrderSummaryRowMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        // orderId, orderDate, totalValue, dispatchDate, deliveryDate map automatically.
        // paymentStatus is an enum in Order but a String in the DTO — convert via .name().
        modelMapper.createTypeMap(Order.class, MerchantOrdersSummary.OrderSummaryRow.class)
                .addMappings(m -> m
                        .using(ctx -> ((Order.PaymentStatus) ctx.getSource()).name())
                        .map(Order::getPaymentStatus, MerchantOrdersSummary.OrderSummaryRow::setPaymentStatus));
    }

    @Override
    public MerchantOrdersSummary.OrderSummaryRow mapTo(Order order) {
        return modelMapper.map(order, MerchantOrdersSummary.OrderSummaryRow.class);
    }

    @Override
    public Order mapFrom(MerchantOrdersSummary.OrderSummaryRow row) {
        return modelMapper.map(row, Order.class);
    }
}
