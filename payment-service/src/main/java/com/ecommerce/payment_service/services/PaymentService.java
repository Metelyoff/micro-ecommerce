package com.ecommerce.payment_service.services;

import com.ecommerce.outbox.annotations.OutboxTransaction;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.payment_service.configs.ExpirationRulePropertyConfig;
import com.ecommerce.payment_service.controllers.PaymentRestController;
import com.ecommerce.payment_service.dtos.*;
import com.ecommerce.payment_service.entities.Payment;
import com.ecommerce.payment_service.entities.Status;
import com.ecommerce.payment_service.repositories.PaymentOutboxRepository;
import com.ecommerce.payment_service.repositories.PaymentRepository;
import com.ecommerce.payment_service.transformers.PaymentDTOTransactionPayloadTransformer;
import com.ecommerce.payment_service.transformers.Transformer;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Predicate;

@Service
public class PaymentService implements PaymentServiceSpec {

    private final PaymentRepository paymentRepository;
    private final Transformer<PaymentRequest, Payment> paymRequestToEntityTransformer;
    private final Transformer<Payment, PaymentDTO> paymentToDTOTransformer;
    private final ExpirationRulePropertyConfig expirationRulePropertyConfig;
    private final PaymentOutboxRepository paymentOutboxRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            Transformer<PaymentRequest, Payment> paymRequestToEntityTransformer,
            Transformer<Payment, PaymentDTO> paymentToDTOTransformer,
            ExpirationRulePropertyConfig expirationRulePropertyConfig,
            PaymentOutboxRepository paymentOutboxRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.paymRequestToEntityTransformer = paymRequestToEntityTransformer;
        this.paymentToDTOTransformer = paymentToDTOTransformer;
        this.expirationRulePropertyConfig = expirationRulePropertyConfig;
        this.paymentOutboxRepository = paymentOutboxRepository;
    }

    @Override
    public Collection<PaymentDTO> findAll() {
        return paymentRepository.findAllProjectedBy(PaymentDTO.class);
    }

    @Override
    public Collection<Payment> findAllByStatus(Status status) {
        return paymentRepository.findAllByStatus(status);
    }

    @Override
    public Collection<PaymentDTO> findAllByOrderId(UUID orderId) {
        return paymentRepository.findAllByOrderId(orderId, PaymentDTO.class);
    }

    @Override
    public PaymentDTO findById(UUID id) {
        return Optional.ofNullable(id)
                .flatMap(paymentRepository::findById)
                .map(paymentToDTOTransformer::transform)
                .orElseThrow(() -> new IllegalArgumentException("Payment with id " + id + " not found"));
    }

    @OutboxTransaction(payloadTransformer = PaymentDTOTransactionPayloadTransformer.class)
    @Override
    public PaymentDTO create(@Valid PaymentRequest request) {
        return Optional.ofNullable(request)
                .map(paymRequestToEntityTransformer::transform)
                .map(this::validateForFailedTransaction)
                .map(this::validateForOrderAlreadyExists)
                .map(this::initializePendingTime)
                .map(paymentRepository::save)
                .map(paymentToDTOTransformer::transform)
                .orElseThrow(() -> new IllegalArgumentException("Payment cannot be null"));
    }

    @SneakyThrows
    private Payment validateForFailedTransaction(Payment payment) {
        boolean alreadyFailed = paymentOutboxRepository.existsByContextIdAndEventName(payment.getOrderId().toString(), "PaymentService.failByOrderId");
        if (alreadyFailed) {
            BindingResult bindingResult = new BeanPropertyBindingResult(payment, "payment");
            bindingResult.addError(new FieldError("payment", "orderId", String.format("Order by id '%s' is failed", payment.getOrderId())));
            Method method = PaymentRestController.class.getDeclaredMethod("create", PaymentRequest.class);
            MethodParameter methodParameter = new MethodParameter(method, 0);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
        return payment;
    }

    @SneakyThrows
    private Payment validateForOrderAlreadyExists(Payment payment) {
        boolean alreadyExistsByOrderId = paymentOutboxRepository.existsByContextIdAndEventName(payment.getOrderId().toString(), "PaymentService.create");
        if (alreadyExistsByOrderId) {
            BindingResult bindingResult = new BeanPropertyBindingResult(payment, "payment");
            bindingResult.addError(new FieldError("payment", "orderId", String.format("Order by id '%s' is already exists", payment.getOrderId())));
            Method method = PaymentRestController.class.getDeclaredMethod("create", PaymentRequest.class);
            MethodParameter methodParameter = new MethodParameter(method, 0);
            throw new MethodArgumentNotValidException(methodParameter, bindingResult);
        }
        return payment;
    }

    private Payment initializePendingTime(Payment payment) {
        if (payment == null) throw new IllegalArgumentException("Payment cannot be null");
        Optional.of(expirationRulePropertyConfig.getDelayMillis())
                .map(delayInMillis -> payment.getCreatedAt().plus(delayInMillis, ChronoUnit.MILLIS))
                .ifPresentOrElse(payment::setExpiredAt, () -> payment.setExpiredAt(payment.getCreatedAt().plusMinutes(15)));
        payment.setStatus(Status.PENDING);
        payment.setStatusDescription(String.format("Please complete the payment before it expires %s", payment.getExpiredAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))));
        return payment;
    }

    @Override
    public boolean isExpired(Payment payment) {
        return payment.getExpiredAt() != null && payment.getExpiredAt().isBefore(LocalDateTime.now());
    }

    @OutboxTransaction(successEvent = "PaymentService.expiredPayment", payloadTransformer = PaymentDTOTransactionPayloadTransformer.class)
    @Override
    public PaymentDTO processExpiredPayment(Payment payment) {
        return Optional.ofNullable(payment)
                .map(this::prepareExpiration)
                .map(paymentRepository::save)
                .map(paymentToDTOTransformer::transform)
                .orElseThrow(() -> new IllegalArgumentException("Payment cannot be null"));
    }

    private Payment findEntityById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment with id " + id + " not found"));
    }

    public static Predicate<Payment> filterByStatus(Status status) {
        return payment -> payment.getStatus().equals(status);
    }

    private Payment prepareExpiration(Payment payment) {
        payment.setStatus(Status.EXPIRED);
        payment.setStatusDescription(expiredStatusDescription(payment.getCreatedAt()));
        return payment;
    }

    @Override
    public void failById(UUID id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @OutboxTransaction
    @Override
    public void failByOrderId(OutboxContext id) {
        Optional.ofNullable(id)
                .map(OutboxContext::getContextId)
                .map(UUID::fromString)
                .map(paymentRepository::findAllByOrderId)
                .stream()
                .flatMap(Collection::stream)
                .filter(PaymentService.filterByStatus(Status.PENDING))
                .map(this::prepareFail)
                .forEach(paymentRepository::save);
    }

    private Payment prepareFail(Payment payment) {
        payment.setStatus(Status.FAILED);
        payment.setStatusDescription("Payment failed");
        payment.setExpiredAt(null);
        return payment;
    }

    @OutboxTransaction
    @Override
    public void cancelById(OutboxContext id) {
        Optional.ofNullable(id)
                .map(OutboxContext::getContextId)
                .map(UUID::fromString)
                .map(this::findEntityById)
                .filter(PaymentService.filterByStatus(Status.PENDING))
                .map(this::prepareCancellation)
                .ifPresent(paymentRepository::save);
    }

    private Payment prepareCancellation(Payment payment) {
        payment.setStatus(Status.CANCELLED);
        payment.setStatusDescription("Payment cancelled");
        payment.setExpiredAt(null);
        return payment;
    }

    private String expiredStatusDescription(LocalDateTime createdAt) {
        String statusDescription = "Payment expired after ";
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        String durationString = duration.toSeconds() < 60
                ? duration.toSeconds() + " seconds"
                : duration.toMinutes() < 60
                ? duration.toMinutes() + " minutes"
                : duration.toHours() < 24
                ? duration.toHours() + " hours"
                : duration.toDays() + " days";
        return statusDescription + durationString;
    }

    @OutboxTransaction
    @Override
    public PaymentDTO pay(UUID id) {
        return Optional.ofNullable(id)
                .flatMap(paymentRepository::findById)
                .map(this::handlePaymentPayStatus)
                .map(paymentRepository::save)
                .map(paymentToDTOTransformer::transform)
                .orElseThrow(() -> new IllegalArgumentException("Payment with id " + id + " not found"));
    }

    private Payment handlePaymentPayStatus(Payment payment) {
        if (isExpired(payment)) {
            prepareExpiration(payment);
        } else {
            payment.setStatus(Status.SUCCESS);
        }
        return payment;
    }

}
