package com.ecommerce.outbox.annotations;

import com.ecommerce.outbox.transformers.OutboxTransactionPayloadTransformer;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Transactional
public @interface OutboxTransaction {

    String name() default "";

    String successEvent() default "";

    String rollbackEvent() default "";

    Class<? extends OutboxTransactionPayloadTransformer> payloadTransformer() default OutboxTransactionPayloadTransformer.class;

}