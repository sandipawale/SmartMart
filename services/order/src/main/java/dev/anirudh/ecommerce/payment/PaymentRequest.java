package dev.yash.ecommerce.payment;

import dev.yash.ecommerce.customer.CustomerResponse;
import dev.yash.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
    BigDecimal amount,
    PaymentMethod paymentMethod,
    Integer orderId,
    String orderReference,
    CustomerResponse customer
){
}
