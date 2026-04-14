package com.ck.PI.orderApi.service;

import com.ck.PI.orderApi.dto.CustomerDetailsDto;
import com.ck.PI.orderApi.dto.ItemDto;
import com.ck.PI.orderApi.dto.request.CancelOrderRequest;
import com.ck.PI.orderApi.dto.request.OrderCreateRequest;
import com.ck.PI.orderApi.dto.response.OrderResponse;
import com.ck.PI.orderApi.entity.CustomerDetails;
import com.ck.PI.orderApi.entity.Item;
import com.ck.PI.orderApi.entity.Order;
import com.ck.PI.orderApi.enums.OrderStatus;
import com.ck.PI.orderApi.exception.BadRequestException;
import com.ck.PI.orderApi.exception.InternalServerException;
import com.ck.PI.orderApi.exception.ResourceNotFoundException;
import com.ck.PI.orderApi.repository.OrderRepository;
import com.ck.PI.orderApi.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void getAllOrders_noStatusFilter_returnsAllOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(
                buildOrder("id-1", OrderStatus.PENDING, 1),
                buildOrder("id-2", OrderStatus.PROCESSING, 2)
        );
        when(orderRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(orders, pageable, orders.size()));

        Page<OrderResponse> result = orderService.getAllOrders(null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getOrderId()).isEqualTo("id-1");
        assertThat(result.getContent().get(1).getOrderId()).isEqualTo("id-2");
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(orderRepository).findAll(pageable);
        verify(orderRepository, never()).findByStatus(any(), any(Pageable.class));
    }

    @Test
    void getAllOrders_withStatusFilter_returnsOnlyMatchingOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> pendingOrders = List.of(buildOrder("id-1", OrderStatus.PENDING, 1));
        when(orderRepository.findByStatus(OrderStatus.PENDING, pageable))
                .thenReturn(new PageImpl<>(pendingOrders, pageable, pendingOrders.size()));

        Page<OrderResponse> result = orderService.getAllOrders(OrderStatus.PENDING, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalPages()).isEqualTo(1);
        verify(orderRepository).findByStatus(OrderStatus.PENDING, pageable);
        verify(orderRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllOrders_repositoryThrows_throwsInternalServerException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable)).thenThrow(new RuntimeException("DB connection lost"));

        assertThatThrownBy(() -> orderService.getAllOrders(null, pageable))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Failed to retrieve orders");
    }

    @Test
    void getOrderByOrderId_existingOrder_returnsCorrectResponse() {
        Order order = buildOrder("id-1", OrderStatus.PROCESSING, 2);
        when(orderRepository.findById("id-1")).thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderByOrderId("id-1");

        assertThat(result.getOrderId()).isEqualTo("id-1");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getCustomerDetails().getName()).isEqualTo("Test User");
    }

    @Test
    void getOrderByOrderId_orderNotFound_throwsResourceNotFoundException() {
        when(orderRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderByOrderId("missing-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing-id");
    }

    @Test
    void getOrderByOrderId_repositoryThrows_throwsInternalServerException() {
        when(orderRepository.findById(anyString())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> orderService.getOrderByOrderId("id-1"))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Failed to retrieve order");
    }

    @Test
    void createOrder_validRequest_returnsPendingOrderWithGeneratedId() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse result = orderService.createOrder(buildCreateRequest());

        assertThat(result.getOrderId()).isNotBlank();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductId()).isEqualTo("PROD-A");
        assertThat(result.getCustomerDetails().getName()).isEqualTo("Jane Doe");
        assertThat(result.getOrderCreatedAt()).isNotNull();
        assertThat(result.getOrderModifiedAt()).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_repositoryThrows_throwsInternalServerException() {
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("DB write failed"));

        assertThatThrownBy(() -> orderService.createOrder(buildCreateRequest()))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Failed to create order");
    }

    @Test
    void cancelOrder_pendingOrder_setsStatusToCancelledAndSaves() {
        Order order = buildOrder("id-1", OrderStatus.PENDING, 1);
        when(orderRepository.findById("id-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse result = orderService.cancelOrder("id-1", new CancelOrderRequest());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void cancelOrder_orderNotFound_throwsResourceNotFoundException() {
        when(orderRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder("missing-id", new CancelOrderRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing-id");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_processingOrder_throwsBadRequestException() {
        Order order = buildOrder("id-1", OrderStatus.PROCESSING, 1);
        when(orderRepository.findById("id-1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder("id-1", new CancelOrderRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("PROCESSING");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_shippedOrder_throwsBadRequestException() {
        Order order = buildOrder("id-2", OrderStatus.SHIPPED, 1);
        when(orderRepository.findById("id-2")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder("id-2", new CancelOrderRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SHIPPED");
    }

    @Test
    void cancelOrder_repositoryThrowsOnSave_throwsInternalServerException() {
        Order order = buildOrder("id-1", OrderStatus.PENDING, 1);
        when(orderRepository.findById("id-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("DB write failed"));

        assertThatThrownBy(() -> orderService.cancelOrder("id-1", new CancelOrderRequest()))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Failed to cancel order");
    }

    private Order buildOrder(String id, OrderStatus status, int numItems) {
        List<Item> items = new java.util.ArrayList<>();
        for (int i = 1; i <= numItems; i++) {
            items.add(Item.builder()
                    .productId("PROD-" + i)
                    .quantity(i)
                    .price(BigDecimal.valueOf(9.99 * i))
                    .build());
        }
        return Order.builder()
                .orderId(id)
                .items(items)
                .customerDetails(CustomerDetails.builder()
                        .name("Test User")
                        .phoneNo("9999999999")
                        .address("1 Test Lane")
                        .build())
                .status(status)
                .orderCreatedAt(LocalDateTime.now())
                .orderModifiedAt(LocalDateTime.now())
                .build();
    }

    private OrderCreateRequest buildCreateRequest() {
        return OrderCreateRequest.builder()
                .items(List.of(
                        ItemDto.builder()
                                .productId("PROD-A")
                                .quantity(2)
                                .price(BigDecimal.valueOf(19.99))
                                .build()
                ))
                .customerDetails(CustomerDetailsDto.builder()
                        .name("Jane Doe")
                        .phoneNo("8888888888")
                        .address("2 Sample St")
                        .build())
                .build();
    }
}
