package com.ecommerce.inventory_service.controllers;

import com.ecommerce.inventory_service.dtos.ItemDTO;
import com.ecommerce.inventory_service.services.ItemServiceSpec;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/items")
public class ItemRestController {

    private final ItemServiceSpec itemServiceSpec;

    public ItemRestController(ItemServiceSpec itemServiceSpec) {
        this.itemServiceSpec = itemServiceSpec;
    }

    @GetMapping
    public ResponseEntity<Collection<ItemDTO>> findAll() {
        return ResponseEntity.ok(itemServiceSpec.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> findById(@PathVariable String id) {
        return ResponseEntity.ok(itemServiceSpec.findById(id));
    }

}
