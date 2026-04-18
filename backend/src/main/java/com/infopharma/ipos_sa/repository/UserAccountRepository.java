package com.infopharma.ipos_sa.repository;

/**
 * UserAccountRepository
 * Spring Data JPA repository for {@link com.infopharma.ipos_sa.entity.UserAccount}.
 * Provides finders by account type (e.g. all merchants), by username (for
 * login lookup), and by account status (e.g. all suspended accounts).
 */
import com.infopharma.ipos_sa.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    List<UserAccount> findByAccountType(UserAccount.AccountType accountType);
    Optional<UserAccount> findByUsername(String username);
    List<UserAccount> findByAccountStatus(UserAccount.AccountStatus accountStatus);
}
