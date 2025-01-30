package com.ecommerce.outbox.core;

import com.ecommerce.outbox.annotations.OutboxTransaction;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.entities.OutboxEvent;
import com.ecommerce.outbox.exceptions.OutboxTransactionException;
import com.ecommerce.outbox.transformers.OutboxTransactionPayloadTransformer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
public class OutboxTransactionAspect {

    @PersistenceContext(type = PersistenceContextType.TRANSACTION)
    private EntityManager entityManager;
    private final ApplicationContext applicationContext;
    private final ApplicationEventPublisher eventPublisher;

    private static final Logger LOG = LoggerFactory.getLogger(OutboxTransactionAspect.class);

    private static final Class<?> DEFAULT_TRANSFORMER_CLASS = OutboxTransactionPayloadTransformer.class;

    private static final String CONTEXT_ID_NOT_SPECIFIED_MESSAGE =
            "Please provide OutboxContext ID for returning value or pass it to the method as the first parameter.";

    private static final String EVENT_NAME_FORMAT = "%s.%s";
    private static final int MAX_TEXT_LENGTH = 255;

    public OutboxTransactionAspect(
            ApplicationContext applicationContext,
            ApplicationEventPublisher eventPublisher
    ) {
        this.applicationContext = applicationContext;
        this.eventPublisher = eventPublisher;
    }


    /**
     * Manages and processes transactions annotated with {@link OutboxTransaction}.
     * <p>
     * This method uses the {@code @Around} advice to intercept and wrap methods annotated with {@link OutboxTransaction}.
     * The advice executes custom logic before and after the target method, such as extracting the necessary context
     * information, applying payload transformation, creating and persisting {@link OutboxEvent} records, and publishing
     * relevant domain events. It ensures error handling by capturing and processing exceptions thrown by the target
     * method. Furthermore, this advice is responsible for:
     * <ul>
     *     <li>Ensuring audit tracking and logging at each phase of the transaction lifecycle.</li>
     *     <li>Validating context IDs for accurate processing.</li>
     *     <li>Generating distinct events for successful transactions and rollbacks based on transactional outcomes.</li>
     * </ul>
     * This advice enables robust transaction management and improves traceability of business events.
     *
     * @param joinPoint         the proceeding join point representing the intercepted method execution context
     * @param outboxTransaction the {@link OutboxTransaction} annotation providing transaction-specific metadata
     * @return the result of the intercepted method execution
     * @throws Throwable if the intercepted method throws any exception or transaction processing fails
     */
    @Around("@annotation(outboxTransaction)")
    public Object manageTransaction(
            ProceedingJoinPoint joinPoint,
            OutboxTransaction outboxTransaction
    ) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object transactionResult;
        String parameterOutboxContextId = extractContextIdFromArguments(joinPoint);
        String transactionProcessName = generateEventName(outboxTransaction, joinPoint, Optional.empty());

        try {
            transactionResult = joinPoint.proceed();
            String contextId = Optional.ofNullable(transactionResult)
                    .filter(OutboxContext.class::isInstance)
                    .map(OutboxContext.class::cast)
                    .map(OutboxContext::getContextId)
                    .orElse(parameterOutboxContextId);

            if (contextId == null) {
                throw new OutboxTransactionException(CONTEXT_ID_NOT_SPECIFIED_MESSAGE);
            }

            String transformedPayload = applyTransformation(outboxTransaction, transactionResult);
            OutboxEvent event = createOutboxEvent(
                    contextId,
                    transactionProcessName,
                    transformedPayload,
                    generateEventName(outboxTransaction, joinPoint, Optional.of(true)),
                    OutboxEventStatus.PROCESSED,
                    startTime,
                    null
            );

            entityManager.persist(event);
            publishEvent(event);
            return transactionResult;
        } catch (Throwable ex) {
            LOG.error("Outbox transaction failed with cause: ", ex);
            publishEvent(createOutboxEvent(
                    parameterOutboxContextId,
                    transactionProcessName,
                    null,
                    generateEventName(outboxTransaction, joinPoint, Optional.of(false)),
                    OutboxEventStatus.FAILED,
                    startTime,
                    ex.getMessage()
            ));
            throw ex;
        } finally {
            logExecutionTime(joinPoint, startTime);
        }
    }

    private String extractContextIdFromArguments(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getArgs().length > 0) {
            return Optional.ofNullable(joinPoint.getArgs()[0])
                    .filter(OutboxContext.class::isInstance)
                    .map(OutboxContext.class::cast)
                    .map(OutboxContext::getContextId)
                    .orElse(null);
        }
        return null;
    }

    private String applyTransformation(OutboxTransaction transaction, Object entity) {
        if (transaction.payloadTransformer().equals(DEFAULT_TRANSFORMER_CLASS)) {
            LOG.info("Will be applied default object.toString() method. Please implement the OutboxDTransactionPayloadTransformer interface.");
            return Optional.ofNullable(entity).map(Object::toString).orElse(null);
        }
        return applicationContext.getBean(transaction.payloadTransformer()).transform(entity);
    }

    private OutboxEvent createOutboxEvent(
            String contextId,
            String processName,
            String payload,
            String eventName,
            OutboxEventStatus status,
            long startTime,
            String statusMessage
    ) {
        return new OutboxEvent(
                Optional.ofNullable(contextId)
                        .map(this::truncateToMaxLength)
                        .orElse(UUID.randomUUID().toString()),
                truncateToMaxLength(processName),
                truncateToMaxLength(eventName),
                payload,
                status,
                Optional.ofNullable(statusMessage)
                        .map(this::truncateToMaxLength)
                        .orElse(null),
                System.currentTimeMillis() - startTime
        );
    }

    private void publishEvent(OutboxEvent event) {
        eventPublisher.publishEvent(event);
    }

    private void logExecutionTime(JoinPoint joinPoint, long startTime) {
        long endTime = System.currentTimeMillis();
        LOG.debug("Method {} took: {}ms", joinPoint.getSignature(), endTime - startTime);
    }

    private String generateEventName(OutboxTransaction transaction, JoinPoint joinPoint, Optional<Boolean> isSuccess) {
        return isSuccess.map(v -> v ? transaction.successEvent() : transaction.rollbackEvent())
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .orElse(String.format(EVENT_NAME_FORMAT,
                        joinPoint.getTarget().getClass().getSimpleName(),
                        joinPoint.getSignature().getName()
                ));
    }

    private String truncateToMaxLength(String text) {
        return (text != null && text.length() > MAX_TEXT_LENGTH)
                ? text.substring(0, MAX_TEXT_LENGTH)
                : text;
    }

}

