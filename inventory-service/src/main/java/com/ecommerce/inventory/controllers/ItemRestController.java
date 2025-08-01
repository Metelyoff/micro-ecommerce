package com.ecommerce.inventory.controllers;

import com.ecommerce.inventory.dtos.ItemDTO;
import com.ecommerce.inventory.services.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemRestController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<Collection<ItemDTO>> findAll() {
        log.debug("Get all items");
        return ResponseEntity.ok(itemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> findById(@PathVariable UUID id) {
        log.debug("Find item by id: {}", id);
        return ResponseEntity.ok(itemService.findById(id));
    }

}
