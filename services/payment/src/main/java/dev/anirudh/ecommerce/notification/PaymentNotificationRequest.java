package dev.yash.ecommerce.notification;

import dev.yash.ecommerce.payment.PaymentMethod;

import java.math.BigDecimal;

public record  PaymentNotificationRequest(
        String orderReference,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String customerFirstName,
        String customerLastName,
        String customerEmail
        ) {
}
