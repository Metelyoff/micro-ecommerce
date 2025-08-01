package com.ecommerce.inventory.exceptions;

public class ItemBadRequestException extends RuntimeException {
    public ItemBadRequestException(String message) {
        super(message);
    }
}

