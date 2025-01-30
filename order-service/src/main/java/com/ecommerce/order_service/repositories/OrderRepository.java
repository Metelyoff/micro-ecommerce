package com.ecommerce.order_service.repositories;

import com.ecommerce.order_service.entities.Order;
import com.ecommerce.order_service.entities.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
    void updateStatus(@Param("orderId") UUID orderId, @Param("status") OrderStatus status);

    @Override
    @EntityGraph(attributePaths = {"items"})
    List<Order> findAll();

    @Override
    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findById(@NonNull UUID id);
}
