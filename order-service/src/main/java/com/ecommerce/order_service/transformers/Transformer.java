package com.ecommerce.order_service.transformers;

public interface Transformer<FROM, TO> {
    TO transform(FROM from);
}
