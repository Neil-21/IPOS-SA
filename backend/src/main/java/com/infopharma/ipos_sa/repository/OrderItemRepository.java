package com.infopharma.ipos_sa.repository;

/**
 * OrderItemRepository
 * Spring Data JPA repository for {@link com.infopharma.ipos_sa.entity.OrderItem}.
 * Custom queries: find items by order, by a list of orders (bulk report
 * queries), or by catalogue item. Includes a native {@code DELETE} query
 * used when a catalogue item is removed to clean up orphaned order lines.
 */
import com.infopharma.ipos_sa.entity.CatalogueItem;
import com.infopharma.ipos_sa.entity.Order;
import com.infopharma.ipos_sa.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrder(Order order);
    List<OrderItem> findByOrderIn(List<Order> orders);
    List<OrderItem> findByItem(CatalogueItem item);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM order_items WHERE item_id = :itemId", nativeQuery = true)
    void deleteByItemId(@Param("itemId") String itemId);
}
