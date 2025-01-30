package com.ecommerce.payment_service.transformers;

import com.ecommerce.payment_service.dtos.PaymentRequest;
import com.ecommerce.payment_service.entities.Payment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentRequestToEntityTransformer implements Transformer<PaymentRequest, Payment> {

    @Override
    public Payment transform(PaymentRequest request) {
        if (request == null) throw new IllegalArgumentException("PaymentRequest cannot be null");
        Payment payment = new Payment();
        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.setOrderId(request.orderId());
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }

}
