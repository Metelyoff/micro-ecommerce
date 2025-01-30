package com.ecommerce.inventory_service.repositories;

import com.ecommerce.inventory_service.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    Collection<Item> findAllByIdIn(Collection<UUID> ids);
}
