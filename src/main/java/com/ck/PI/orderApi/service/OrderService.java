package com.ck.PI.orderApi.service;

import com.ck.PI.orderApi.dto.request.CancelOrderRequest;
import com.ck.PI.orderApi.dto.request.OrderCreateRequest;
import com.ck.PI.orderApi.dto.response.OrderResponse;
import com.ck.PI.orderApi.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    List<OrderResponse> getAllOrders(OrderStatus status);

    OrderResponse getOrderByOrderId(String orderId);

    OrderResponse createOrder(OrderCreateRequest request);

    OrderResponse cancelOrder(String orderId, CancelOrderRequest request);
}
