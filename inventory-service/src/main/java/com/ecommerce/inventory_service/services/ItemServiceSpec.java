package com.ecommerce.inventory_service.services;

import com.ecommerce.inventory_service.dtos.ItemDTO;

import java.util.Collection;

public interface ItemServiceSpec {
    Collection<ItemDTO> findAll();
    Collection<ItemDTO> findAllByIds(Collection<String> ids);
    ItemDTO findById(String id);
    ItemDTO save(ItemDTO itemDTO);
}
