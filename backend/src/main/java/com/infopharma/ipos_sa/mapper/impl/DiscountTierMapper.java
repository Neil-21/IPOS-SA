package com.infopharma.ipos_sa.mapper.impl;

/**
 * DiscountTierMapper
 * Maps between a {@link com.infopharma.ipos_sa.entity.DiscountTier} entity
 * and a {@link com.infopharma.ipos_sa.dto.DiscountPlanRequest.TierRequest}
 * DTO, used when reading or updating flexible discount plan tiers.
 */
import com.infopharma.ipos_sa.dto.DiscountPlanRequest;
import com.infopharma.ipos_sa.entity.DiscountTier;
import com.infopharma.ipos_sa.mapper.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class DiscountTierMapper implements Mapper<DiscountTier, DiscountPlanRequest.TierRequest> {

    private final ModelMapper modelMapper;

    public DiscountTierMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public DiscountPlanRequest.TierRequest mapTo(DiscountTier tier) {
        return modelMapper.map(tier, DiscountPlanRequest.TierRequest.class);
    }

    @Override
    public DiscountTier mapFrom(DiscountPlanRequest.TierRequest tierRequest) {
        return modelMapper.map(tierRequest, DiscountTier.class);
    }
}
