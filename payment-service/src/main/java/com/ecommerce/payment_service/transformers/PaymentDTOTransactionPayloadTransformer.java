package com.ecommerce.payment_service.transformers;

import com.ecommerce.outbox.transformers.OutboxTransactionPayloadTransformer;
import com.ecommerce.payment_service.dtos.PaymentDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PaymentDTOTransactionPayloadTransformer implements OutboxTransactionPayloadTransformer<PaymentDTO> {

    private final ObjectMapper objectMapper;

    public PaymentDTOTransactionPayloadTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String transform(PaymentDTO paymentDTO) {
        try {
            return objectMapper.writeValueAsString(paymentDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
