package com.ecommerce.order_service.repositories;

import com.ecommerce.order_service.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<OrderItem, UUID> {
}
