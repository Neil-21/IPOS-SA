package com.infopharma.ipos_sa.dto;

/**
 * CreateUserAccountRequest
 * DTO used to create a new user account (merchant or staff).
 * Carries all fields required to populate a {@link com.infopharma.ipos_sa.entity.UserAccount},
 * including account type, credentials, company details, and credit limit.
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infopharma.ipos_sa.entity.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserAccountRequest {

    private String username;
    private String password;
    private UserAccount.AccountType accountType;
    private UserAccount.AccountStatus accountStatus;
    private String contactName;
    private String companyName;
    private String address;
    private String phone;
    private String fax;
    private String email;
    private BigDecimal creditLimit;
}
