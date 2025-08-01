package com.ecommerce.inventory.repositories;

import com.ecommerce.inventory.entities.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {
    Collection<ItemEntity> findAllByIdIn(Collection<UUID> ids);
}
