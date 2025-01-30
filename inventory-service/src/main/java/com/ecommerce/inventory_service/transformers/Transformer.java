package com.ecommerce.inventory_service.transformers;

public interface Transformer<FROM, TO> {
    TO transform(FROM from);
}
