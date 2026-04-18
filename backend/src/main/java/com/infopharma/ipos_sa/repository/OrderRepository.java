package com.infopharma.ipos_sa.repository;

/**
 * OrderRepository
 * Spring Data JPA repository for {@link com.infopharma.ipos_sa.entity.Order}.
 * Provides finders by merchant account, by order date range, and by status
 * exclusion — used by the order service and reporting layer.
 */
import com.infopharma.ipos_sa.entity.Order;
import com.infopharma.ipos_sa.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByAccount(UserAccount account);
    List<Order> findByAccountAndOrderDateBetween(UserAccount account, LocalDate from, LocalDate to);
    List<Order> findByOrderDateBetween(LocalDate from, LocalDate to);
    List<Order> findByStatusNot(Order.OrderStatus status);
}
