package com.ecommerce.inventory.exceptions;

import java.util.UUID;

public class ItemNotFoundException extends RuntimeException {

    public static final String NOT_FOUND_BY_ID_PATTERN = "Item with id %s not found";

    public ItemNotFoundException(UUID id) {
        super(String.format(NOT_FOUND_BY_ID_PATTERN, id));
    }

    public ItemNotFoundException(String message) {
        super(message);
    }

}
