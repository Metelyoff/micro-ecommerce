package com.ecommerce.payment_service.services;

import com.ecommerce.payment_service.entities.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DemoExpirationDelayChecker implements ExpirationDelayChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DemoExpirationDelayChecker.class);

    private final PaymentServiceSpec paymentServiceSpec;

    public DemoExpirationDelayChecker(
            PaymentServiceSpec paymentServiceSpec
    ) {
        this.paymentServiceSpec = paymentServiceSpec;
    }

    @Scheduled(fixedRateString = "#{@expirationRulePropertyConfig.checkerMillis}")
    @Override
    public void checkPaymentExpirationDelay() {
        LOG.debug("Start checking payment expiration delay");
        paymentServiceSpec.findAllByStatus(Status.PENDING).stream()
                .filter(paymentServiceSpec::isExpired)
                .forEach(paymentServiceSpec::processExpiredPayment);
        LOG.debug("End checking payment expiration delay");
    }

}
