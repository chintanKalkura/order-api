package com.ck.PI.orderApi.controller;

import com.ck.PI.orderApi.dto.request.CancelOrderRequest;
import com.ck.PI.orderApi.dto.request.OrderCreateRequest;
import com.ck.PI.orderApi.dto.response.OrderResponse;
import com.ck.PI.orderApi.enums.OrderStatus;
import com.ck.PI.orderApi.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "orderCreatedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<OrderResponse> orders = orderService.getAllOrders(status, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderByOrderId(@PathVariable String orderId) {
        OrderResponse order = orderService.getOrderByOrderId(orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request) {
        OrderResponse createdOrder = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) CancelOrderRequest request) {
        CancelOrderRequest cancelRequest = request != null ? request : new CancelOrderRequest();
        OrderResponse cancelledOrder = orderService.cancelOrder(orderId, cancelRequest);
        return ResponseEntity.ok(cancelledOrder);
    }
}
