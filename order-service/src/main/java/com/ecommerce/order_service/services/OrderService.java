package com.ecommerce.order_service.services;

import com.ecommerce.order_service.dtos.OrderDTO;
import com.ecommerce.order_service.dtos.OrderRequest;
import com.ecommerce.order_service.entities.Order;
import com.ecommerce.order_service.entities.OrderItem;
import com.ecommerce.order_service.entities.OrderStatus;
import com.ecommerce.order_service.repositories.OrderRepository;
import com.ecommerce.order_service.transformers.OrderOutboxTransactionTransformer;
import com.ecommerce.order_service.transformers.Transformer;
import com.ecommerce.outbox.annotations.OutboxTransaction;
import com.ecommerce.outbox.core.OutboxContext;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService implements OrderServiceSpec {

    private final OrderRepository orderRepository;
    private final Transformer<OrderRequest, Order> orderRequestToEntityTransformer;
    private final Transformer<Order, OrderDTO> orderToDTOTransformer;
    private final SSEService<OrderDTO> sseService;

    public OrderService(
            OrderRepository orderRepository,
            Transformer<OrderRequest, Order> orderRequestToEntityTransformer,
            Transformer<Order, OrderDTO> orderToDTOTransformer,
            SSEService<OrderDTO> sseService
    ) {
        this.orderRepository = orderRepository;
        this.orderRequestToEntityTransformer = orderRequestToEntityTransformer;
        this.orderToDTOTransformer = orderToDTOTransformer;
        this.sseService = sseService;
    }

    @Override
    public Collection<OrderDTO> findAll() {
        return orderRepository.findAll().stream()
                .map(orderToDTOTransformer::transform)
                .collect(Collectors.toSet());
    }

    @Override
    public OrderDTO findById(UUID id) {
        return Optional.ofNullable(id)
                .flatMap(orderRepository::findById)
                .map(orderToDTOTransformer::transform)
                .orElseThrow(() -> new IllegalArgumentException("Order with id " + id + " not found"));
    }

    @OutboxTransaction(payloadTransformer = OrderOutboxTransactionTransformer.class)
    @Override
    public OrderDTO create(OrderRequest orderRequest) {
        return Optional.ofNullable(orderRequest)
                .map(orderRequestToEntityTransformer::transform)
                .map(this::handleCreatedOrder)
                .map(orderRepository::save)
                .map(orderToDTOTransformer::transform)
                .map(this::sendNextSSE)
                .orElseThrow(() -> new IllegalArgumentException("Order request cannot be null"));
    }

    private Order handleCreatedOrder(Order order) {

        int totalItems = Optional.ofNullable(order.getItems())
                .stream()
                .flatMap(Collection::stream)
                .mapToInt(OrderItem::getQuantity)
                .sum();
        order.setTotalItems(totalItems);

        BigDecimal totalPrice = Optional.ofNullable(order.getItems())
                .stream()
                .flatMap(Collection::stream)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        order.setTotalPrice(totalPrice);

        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        return order;
    }

    private OrderDTO sendNextSSE(OrderDTO order) {
        sseService.emit(order.id(), order);
        return order;
    }

    @OutboxTransaction
    @Override
    public void cancelByReason(OutboxContext id, String reason) {
        Optional.ofNullable(id)
                .map(OutboxContext::getContextId)
                .map(UUID::fromString)
                .flatMap(orderRepository::findById)
                .map(this::validateOrderForCancel)
                .map(order -> prepareCancelWithReason(order, reason))
                .map(orderRepository::save)
                .map(this::sendFinalSSE)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Order with id %s not found", Optional.ofNullable(id).map(OutboxContext::getContextId).orElse(null))));
    }

    @SneakyThrows
    private Order validateOrderForCancel(Order order) {
        if (!order.getStatus().isPending()) {
            throw new IllegalArgumentException(String.format("Order status '%s' is invalid for cancellation", order.getStatus()));
        }
        return order;
    }

    private Order prepareCancelWithReason(Order order, String reason) {
        order.setStatus(OrderStatus.CANCELLED);
        order.setStatusDescription(String.format("Order cancelled by reason: %s", reason));
        return order;
    }

    private Order sendFinalSSE(Order order) {
        sseService.emit(order.getId(), orderToDTOTransformer.transform(order));
        sseService.complete(order.getId());
        return order;
    }

    @OutboxTransaction
    @Override
    public void fail(OutboxContext id, String reason) {
        Optional.ofNullable(id)
                .map(OutboxContext::getContextId)
                .map(UUID::fromString)
                .flatMap(orderRepository::findById)
                .map(order -> prepareFail(order, reason))
                .map(orderRepository::save)
                .map(this::sendFinalSSE)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Order with id %s not found", id)));
    }

    private Order prepareFail(Order order, String reason) {
        order.setStatus(OrderStatus.FAILED);
        order.setStatusDescription(reason);
        return order;
    }

    @OutboxTransaction(payloadTransformer = OrderOutboxTransactionTransformer.class)
    @Override
    public OrderDTO confirmReservation(UUID id) {
        return Optional.ofNullable(id)
                .flatMap(orderRepository::findById)
                .map(this::prepareReservation)
                .map(orderRepository::save)
                .map(orderToDTOTransformer::transform)
                .map(this::sendNextSSE)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Order with id %s not found", id)));
    }

    @OutboxTransaction(payloadTransformer = OrderOutboxTransactionTransformer.class)
    @Override
    public OrderDTO confirmPayment(OutboxContext orderId, UUID paymentId, String statusDescription) {
        return Optional.ofNullable(orderId)
                .map(OutboxContext::getContextId)
                .map(UUID::fromString)
                .flatMap(orderRepository::findById)
                .map(this::validateOrderForPaymentConfirmation)
                .map(order -> prepareCreatedPayment(order, paymentId, statusDescription))
                .map(orderRepository::save)
                .map(orderToDTOTransformer::transform)
                .map(this::sendNextSSE)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Order with id %s not found",
                        Optional.ofNullable(orderId).map(OutboxContext::getContextId).orElse(null))
                ));
    }

    @SneakyThrows
    private Order validateOrderForPaymentConfirmation(Order order) {
        if (!OrderStatus.RESERVED.equals(order.getStatus())) {
            throw new IllegalArgumentException(String.format("Order status '%s' is invalid for payment confirmation", order.getStatus()));
        }
        return order;
    }

    @OutboxTransaction
    @Override
    public void confirmPaidPayment(OutboxContext orderId) {
        Optional.ofNullable(orderId)
                .map(OutboxContext::getContextId)
                .map(UUID::fromString)
                .flatMap(orderRepository::findById)
                .map(this::preparePaidPayment)
                .map(orderRepository::save)
                .map(this::sendFinalSSE)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Order with id %s not found", orderId)));
    }

    private Order preparePaidPayment(Order order) {
        order.setStatus(OrderStatus.PAID);
        order.setStatusDescription("Order successfully paid. Thank you for the order :)");
        return order;
    }

    private Order prepareCreatedPayment(Order order, UUID paymentId, String statusDescription) {
        order.setPaymentId(paymentId);
        order.setStatusDescription(statusDescription);
        order.setStatus(OrderStatus.PENDING_FOR_PAY);
        return order;
    }

    private Order prepareReservation(Order order) {
        order.setStatus(OrderStatus.RESERVED);
        order.setStatusDescription("All items successfully reserved");
        return order;
    }

}
