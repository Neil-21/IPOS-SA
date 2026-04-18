package com.infopharma.ipos_sa.mapper.impl;

/**
 * TurnoverRowMapper
 * Maps an {@link com.infopharma.ipos_sa.entity.Order} to a
 * {@link com.infopharma.ipos_sa.dto.TurnoverReport.TurnoverRow}.
 * Custom TypeMap flattens the nested {@code account.accountId} and
 * {@code account.companyName} fields onto the flat DTO row.
 */
import com.infopharma.ipos_sa.dto.TurnoverReport;
import com.infopharma.ipos_sa.entity.Order;
import com.infopharma.ipos_sa.mapper.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class TurnoverRowMapper implements Mapper<Order, TurnoverReport.TurnoverRow> {

    private final ModelMapper modelMapper;

    public TurnoverRowMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        // orderId, orderDate, totalValue map automatically (exact name match).
        // account is a nested entity — flatten account.accountId and account.companyName.
        modelMapper.createTypeMap(Order.class, TurnoverReport.TurnoverRow.class)
                .addMappings(m -> {
                    m.map(src -> src.getAccount().getAccountId(),   TurnoverReport.TurnoverRow::setAccountId);
                    m.map(src -> src.getAccount().getCompanyName(), TurnoverReport.TurnoverRow::setCompanyName);
                });
    }

    @Override
    public TurnoverReport.TurnoverRow mapTo(Order order) {
        return modelMapper.map(order, TurnoverReport.TurnoverRow.class);
    }

    @Override
    public Order mapFrom(TurnoverReport.TurnoverRow row) {
        return modelMapper.map(row, Order.class);
    }
}
