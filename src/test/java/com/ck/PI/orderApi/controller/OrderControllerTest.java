package com.ck.PI.orderApi.controller;

import com.ck.PI.orderApi.dto.CustomerDetailsDto;
import com.ck.PI.orderApi.dto.ItemDto;
import com.ck.PI.orderApi.dto.request.CancelOrderRequest;
import com.ck.PI.orderApi.dto.request.OrderCreateRequest;
import com.ck.PI.orderApi.dto.response.OrderResponse;
import com.ck.PI.orderApi.enums.OrderStatus;
import com.ck.PI.orderApi.exception.BadRequestException;
import com.ck.PI.orderApi.exception.InternalServerException;
import com.ck.PI.orderApi.exception.ResourceNotFoundException;
import com.ck.PI.orderApi.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllOrders_noFilter_returns200WithOrderList() throws Exception {
        when(orderService.getAllOrders(null))
                .thenReturn(List.of(
                        buildOrderResponse("id-1", OrderStatus.PENDING),
                        buildOrderResponse("id-2", OrderStatus.DELIVERED)
                ));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value("id-1"))
                .andExpect(jsonPath("$[1].orderId").value("id-2"));
    }

    @Test
    void getAllOrders_withStatusFilter_returns200WithFilteredList() throws Exception {
        when(orderService.getAllOrders(OrderStatus.PENDING))
                .thenReturn(List.of(buildOrderResponse("id-1", OrderStatus.PENDING)));

        mockMvc.perform(get("/api/v1/orders").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getAllOrders_invalidStatusParam_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("status", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void getOrderByOrderId_existingOrder_returns200WithOrderResponse() throws Exception {
        when(orderService.getOrderByOrderId("id-1"))
                .thenReturn(buildOrderResponse("id-1", OrderStatus.PROCESSING));

        mockMvc.perform(get("/api/v1/orders/id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("id-1"))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.customerDetails.name").value("Test User"));
    }

    @Test
    void getOrderByOrderId_orderNotFound_returns404WithErrorBody() throws Exception {
        when(orderService.getOrderByOrderId("missing"))
                .thenThrow(new ResourceNotFoundException("Order not found with ID: missing"));

        mockMvc.perform(get("/api/v1/orders/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Order not found with ID: missing"));
    }

    @Test
    void getOrderByOrderId_serviceError_returns500WithErrorBody() throws Exception {
        when(orderService.getOrderByOrderId("id-err"))
                .thenThrow(new InternalServerException("Failed to retrieve order: DB error"));

        mockMvc.perform(get("/api/v1/orders/id-err"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    @Test
    void createOrder_validRequest_returns201WithCreatedOrder() throws Exception {
        when(orderService.createOrder(any(OrderCreateRequest.class)))
                .thenReturn(buildOrderResponse("new-id", OrderStatus.PENDING));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("new-id"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createOrder_serviceError_returns500() throws Exception {
        when(orderService.createOrder(any(OrderCreateRequest.class)))
                .thenThrow(new InternalServerException("Failed to create order: DB write failed"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidCreateRequest())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    @Test
    void createOrder_emptyItemsList_returns400WithValidationMessage() throws Exception {
        OrderCreateRequest badRequest = OrderCreateRequest.builder()
                .items(List.of())
                .customerDetails(CustomerDetailsDto.builder()
                        .name("Jane").phoneNo("123").address("Addr").build())
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Order must contain at least one item"));
    }

    @Test
    void cancelOrder_pendingOrder_returns200WithCancelledStatus() throws Exception {
        when(orderService.cancelOrder(eq("id-1"), any(CancelOrderRequest.class)))
                .thenReturn(buildOrderResponse("id-1", OrderStatus.CANCELLED));

        mockMvc.perform(put("/api/v1/orders/id-1/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("id-1"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelOrder_orderNotFound_returns404() throws Exception {
        when(orderService.cancelOrder(eq("missing"), any()))
                .thenThrow(new ResourceNotFoundException("Order not found with ID: missing"));

        mockMvc.perform(put("/api/v1/orders/missing/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void cancelOrder_orderNotPending_returns400WithMessage() throws Exception {
        when(orderService.cancelOrder(eq("id-2"), any()))
                .thenThrow(new BadRequestException(
                        "Order cannot be cancelled. Only PENDING orders can be cancelled. Current status: PROCESSING"));

        mockMvc.perform(put("/api/v1/orders/id-2/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value(
                        "Order cannot be cancelled. Only PENDING orders can be cancelled. Current status: PROCESSING"));
    }

    private OrderResponse buildOrderResponse(String id, OrderStatus status) {
        return OrderResponse.builder()
                .orderId(id)
                .items(List.of(ItemDto.builder()
                        .productId("PROD-1")
                        .quantity(1)
                        .price(BigDecimal.valueOf(29.99))
                        .build()))
                .customerDetails(CustomerDetailsDto.builder()
                        .name("Test User")
                        .phoneNo("9999999999")
                        .address("1 Test Lane")
                        .build())
                .status(status)
                .orderCreatedAt(LocalDateTime.now())
                .orderModifiedAt(LocalDateTime.now())
                .build();
    }

    private OrderCreateRequest buildValidCreateRequest() {
        return OrderCreateRequest.builder()
                .items(List.of(ItemDto.builder()
                        .productId("PROD-ID-1")
                        .productName("PROD-A")
                        .quantity(2)
                        .price(BigDecimal.valueOf(49.99))
                        .build()))
                .customerDetails(CustomerDetailsDto.builder()
                        .name("Jane Doe")
                        .phoneNo("8888888888")
                        .address("2 Sample St")
                        .build())
                .build();
    }
}