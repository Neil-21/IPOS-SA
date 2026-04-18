package com.infopharma.ipos_sa.repository;

/**
 * PaymentRepository
 * Spring Data JPA repository for {@link com.infopharma.ipos_sa.entity.Payment}.
 * Provides a finder by merchant account used to retrieve all payment records
 * for a given merchant.
 */
import com.infopharma.ipos_sa.entity.Payment;
import com.infopharma.ipos_sa.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByAccount(UserAccount account);
}
