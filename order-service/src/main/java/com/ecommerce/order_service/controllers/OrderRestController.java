package com.ecommerce.order_service.controllers;

import com.ecommerce.order_service.dtos.OrderItemDTO;
import com.ecommerce.order_service.dtos.OrderDTO;
import com.ecommerce.order_service.dtos.OrderRequest;
import com.ecommerce.order_service.services.OrderServiceSpec;
import com.ecommerce.order_service.services.SSEService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import static com.ecommerce.order_service.controllers.OrderRestController.ORDER_PATH;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(ORDER_PATH)
public class OrderRestController {

    public static final String ORDER_PATH = "/orders";

    private final OrderServiceSpec orderServiceSpec;
    private final SSEService<OrderDTO> orderSinks;

    public OrderRestController(
            OrderServiceSpec orderServiceSpec,
            SSEService<OrderDTO> orderSinks
    ) {
        this.orderServiceSpec = orderServiceSpec;
        this.orderSinks = orderSinks;
    }

    @GetMapping
    public ResponseEntity<Collection<OrderDTO>> findAll() {
        return ResponseEntity.ok(orderServiceSpec.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderServiceSpec.findById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(@Valid @RequestBody OrderRequest orderRequest) {
        OrderDTO createdOrder = orderServiceSpec.create(orderRequest);
        URI location = URI.create(String.format("%s/%s", ORDER_PATH, createdOrder.id()));
        return ResponseEntity.created(location)
                .header("Access-Control-Expose-Headers", "Location")
                .build();
    }

    @GetMapping(path = "/subscribe/{orderId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OrderDTO> subscribe(@PathVariable UUID orderId) {
        return orderSinks.subscribe(orderId);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<Void> addItem(
            @PathVariable UUID id,
            @RequestBody OrderItemDTO item
    ) {
        //TODO: implement
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> updateItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestBody OrderItemDTO item
    ) {
        //TODO: implement
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        orderServiceSpec.cancelByReason(id::toString, "Customer cancelled");
        return ResponseEntity.noContent().build();
    }

}
