package com.ecommerce.payment_service.controllers;

import com.ecommerce.payment_service.dtos.PaymentDTO;
import com.ecommerce.payment_service.dtos.PaymentRequest;
import com.ecommerce.payment_service.services.PaymentServiceSpec;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import static com.ecommerce.payment_service.controllers.PaymentRestController.PAYMENT_ENDPOINT;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(PAYMENT_ENDPOINT)
public class PaymentRestController {

    public final static String PAYMENT_ENDPOINT = "/payments";

    private final PaymentServiceSpec paymentService;

    public PaymentRestController(PaymentServiceSpec paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<Collection<PaymentDTO>> findAll() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody PaymentRequest paymentRequest) {
        return ResponseEntity.created(URI.create(PAYMENT_ENDPOINT + "/" + paymentService.create(paymentRequest).id())).build();
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentDTO> pay(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.pay(id));
    }

}
