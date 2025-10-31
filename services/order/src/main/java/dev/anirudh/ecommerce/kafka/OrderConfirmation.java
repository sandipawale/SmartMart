package dev.yash.ecommerce.kafka;

import dev.yash.ecommerce.customer.CustomerResponse;
import dev.yash.ecommerce.order.PaymentMethod;
import dev.yash.ecommerce.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products
) {
}
