package dev.yash.ecommerce.order;

import dev.yash.ecommerce.customer.CustomerClient;
import dev.yash.ecommerce.exception.BusinessException;
import dev.yash.ecommerce.kafka.OrderConfirmation;
import dev.yash.ecommerce.kafka.OrderProducer;
import dev.yash.ecommerce.orderline.OrderLineRequest;
import dev.yash.ecommerce.orderline.OrderLineService;
import dev.yash.ecommerce.payment.PaymentClient;
import dev.yash.ecommerce.payment.PaymentRequest;
import dev.yash.ecommerce.product.ProductClient;
import dev.yash.ecommerce.product.PurchaseRequest;
import dev.yash.ecommerce.redis.RedisService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderMapper mapper;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;
    private final PaymentClient paymentClient;
    private final RedisService redisService;

    public Integer createOrder(@Valid OrderRequest request) {
        var customer = this.customerClient.findCustomerById(request.customerId())
                            .orElseThrow(() -> new BusinessException("Cannot create order:: No customer exists with the provided id."));

        var purchasedProducts = this.productClient.purchaseProducts(request.products());
        var order = this.repository.save(mapper.toOrder(request));

        for(PurchaseRequest purchaseRequest: request.products()){
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }
        var paymentRequest = new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);

        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        request.reference(),
                        request.amount(),
                        request.paymentMethod(),
                        customer,
                        purchasedProducts
                )
        );

        return order.getId();
    }

    public List<OrderResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse findById(Integer orderId) {
        String redisKey = "order:id:" + orderId;

        OrderResponse cacheOrderResponse = redisService.get(redisKey, OrderResponse.class);
        if (cacheOrderResponse != null) {
            return cacheOrderResponse;
        }

        OrderResponse dbOrderResponse = repository.findById(orderId)
                                                .map(mapper::fromOrder)
                                                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with provided Id: %d", orderId)));

        redisService.set(redisKey, dbOrderResponse, 300l);

        return dbOrderResponse;
    }
}