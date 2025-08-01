package com.ecommerce.inventory.services;

import com.ecommerce.inventory.dtos.ItemDTO;

import java.util.Collection;
import java.util.UUID;

public interface ItemService {
    Collection<ItemDTO> findAll();
    Collection<ItemDTO> findAllByIds(Collection<UUID> ids);
    ItemDTO findById(UUID id);
    ItemDTO save(ItemDTO itemDTO);
}
