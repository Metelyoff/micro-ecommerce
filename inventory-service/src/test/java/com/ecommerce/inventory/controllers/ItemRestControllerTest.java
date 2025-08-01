package com.ecommerce.inventory.controllers;

import com.ecommerce.inventory.dtos.ItemDTO;
import com.ecommerce.inventory.exceptions.ItemBadRequestException;
import com.ecommerce.inventory.exceptions.ItemNotFoundException;
import com.ecommerce.inventory.services.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.ecommerce.inventory.exceptions.ItemNotFoundException.NOT_FOUND_BY_ID_PATTERN;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ItemRestController.class)
class ItemRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Test
    @DisplayName("GET /items - should return empty items")
    void findAll_shouldReturnEmptyItems() throws Exception {

        when(itemService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
        ;

        verify(itemService, only()).findAll();
    }

    @Test
    @DisplayName("GET /items - should return all items")
    void findAll_shouldReturnAllItems() throws Exception {
        var items = List.of(
                new ItemDTO("Item A", "image1", 9.99, 5),
                new ItemDTO("Item B", "image2", 20.00, 0)
        );

        when(itemService.findAll()).thenReturn(items);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))

                .andExpect(jsonPath("$[0].name", is("Item A")))
                .andExpect(jsonPath("$[0].image", is("image1")))
                .andExpect(jsonPath("$[0].price", is(9.99)))
                .andExpect(jsonPath("$[0].quantity", is(5)))

                .andExpect(jsonPath("$[1].name", is("Item B")))
                .andExpect(jsonPath("$[1].image", is("image2")))
                .andExpect(jsonPath("$[1].price", is(20.00)))
                .andExpect(jsonPath("$[1].quantity", is(0)))
        ;

        verify(itemService, only()).findAll();
    }

    @Test
    @DisplayName("GET /items/{id} - should return item by id")
    void findById_shouldReturnItem() throws Exception {
        var id = UUID.randomUUID();

        var item = ItemDTO.builder()
                .id(id)
                .name("Item A")
                .image("image1")
                .price(9.99)
                .quantity(5)
                .build();

        when(itemService.findById(id)).thenReturn(item);

        mockMvc.perform(get("/items/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.name", is("Item A")))
                .andExpect(jsonPath("$.image", is("image1")))
                .andExpect(jsonPath("$.price", is(9.99)))
                .andExpect(jsonPath("$.quantity", is(5)))
        ;

        verify(itemService, only()).findById(any());
    }

    @Test
    @DisplayName("GET /items/{id} - should return ItemBadRequestException")
    void findById_shouldReturnItemBadRequestException() throws Exception {

        UUID id = null;
        String errorMessage = "Item id required";

        when(itemService.findById(id)).thenThrow(new ItemBadRequestException(errorMessage));

        mockMvc.perform(get("/items/" + id))
                .andExpect(status().isBadRequest())
        ;

        verify(itemService, never()).findById(id);
    }

    @Test
    @DisplayName("GET /items/{id} - should return ItemNotFoundException")
    void findById_shouldReturnItemNotFoundException() throws Exception {

        UUID id = UUID.randomUUID();
        String errorMessage = String.format(NOT_FOUND_BY_ID_PATTERN, id);

        when(itemService.findById(id)).thenThrow(new ItemNotFoundException(id));

        mockMvc.perform(get("/items/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", is(errorMessage)))
        ;

        verify(itemService, only()).findById(id);
    }

}