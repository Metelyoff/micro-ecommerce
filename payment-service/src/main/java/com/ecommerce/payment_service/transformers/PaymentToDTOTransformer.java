package com.ecommerce.payment_service.transformers;

import com.ecommerce.payment_service.dtos.PaymentDTO;
import com.ecommerce.payment_service.entities.Payment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentToDTOTransformer implements Transformer<Payment, PaymentDTO> {

    @Override
    public PaymentDTO transform(Payment payment) {
        return Optional.ofNullable(payment).map(p -> new PaymentDTO(
                        p.getId(),
                        p.getAmount(),
                        p.getMethod(),
                        p.getStatus(),
                        p.getOrderId(),
                        p.getStatusDescription(),
                        p.getExpiredAt(),
                        p.getCreatedAt()
                )
        ).orElse(null);
    }

}
