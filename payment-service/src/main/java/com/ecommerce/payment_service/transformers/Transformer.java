package com.ecommerce.payment_service.transformers;

public interface Transformer<FROM, TO> {
    TO transform(FROM from);
}
