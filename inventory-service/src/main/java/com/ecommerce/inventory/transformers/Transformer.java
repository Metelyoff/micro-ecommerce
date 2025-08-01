package com.ecommerce.inventory.transformers;

public interface Transformer<FROM, TO> {
    TO transform(FROM from);
}
