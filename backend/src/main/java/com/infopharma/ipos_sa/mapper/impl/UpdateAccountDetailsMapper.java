package com.infopharma.ipos_sa.mapper.impl;

/**
 * UpdateAccountDetailsMapper
 * Maps between a {@link com.infopharma.ipos_sa.entity.UserAccount} and an
 * {@link com.infopharma.ipos_sa.dto.UpdateAccountDetailsRequest}, used when
 * updating an account's type and status fields.
 */
import com.infopharma.ipos_sa.dto.UpdateAccountDetailsRequest;
import com.infopharma.ipos_sa.entity.UserAccount;
import com.infopharma.ipos_sa.mapper.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UpdateAccountDetailsMapper implements Mapper<UserAccount, UpdateAccountDetailsRequest> {

    private final ModelMapper modelMapper;

    public UpdateAccountDetailsMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public UpdateAccountDetailsRequest mapTo(UserAccount userAccount) {
        return modelMapper.map(userAccount, UpdateAccountDetailsRequest.class);
    }

    @Override
    public UserAccount mapFrom(UpdateAccountDetailsRequest dto) {
        return modelMapper.map(dto, UserAccount.class);
    }
}
