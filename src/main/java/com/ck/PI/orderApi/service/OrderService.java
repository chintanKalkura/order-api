package com.ck.PI.orderApi.service;

import com.ck.PI.orderApi.dto.request.CancelOrderRequest;
import com.ck.PI.orderApi.dto.request.OrderCreateRequest;
import com.ck.PI.orderApi.dto.response.OrderResponse;
import com.ck.PI.orderApi.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable);

    OrderResponse getOrderByOrderId(String orderId);

    OrderResponse createOrder(OrderCreateRequest request);

    OrderResponse cancelOrder(String orderId, CancelOrderRequest request);
}
