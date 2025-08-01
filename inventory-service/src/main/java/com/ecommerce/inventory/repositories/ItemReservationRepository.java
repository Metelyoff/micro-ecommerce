package com.ecommerce.inventory.repositories;

import com.ecommerce.inventory.entities.ReservedItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface ItemReservationRepository extends JpaRepository<ReservedItemEntity, UUID> {
    Collection<ReservedItemEntity> findAllByOrderId(UUID orderId);
}
