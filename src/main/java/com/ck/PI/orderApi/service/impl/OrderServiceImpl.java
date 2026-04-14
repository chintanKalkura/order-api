package com.ck.PI.orderApi.service.impl;

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
import com.ck.PI.orderApi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public List<OrderResponse> getAllOrders(OrderStatus status) {
        try {
            List<Order> orders;
            if (status != null) {
                orders = orderRepository.findByStatus(status);
            } else {
                orders = orderRepository.findAll();
            }
            return orders.stream()
                    .map(this::toOrderResponse)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new InternalServerException("Failed to retrieve orders: " + ex.getMessage());
        }
    }

    @Override
    public OrderResponse getOrderByOrderId(String orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Order not found with ID: " + orderId));
            return toOrderResponse(order);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalServerException("Failed to retrieve order: " + ex.getMessage());
        }
    }

    @Override
    public OrderResponse createOrder(OrderCreateRequest request) {
        try {
            List<Item> items = request.getItems().stream()
                    .map(this::toItemEntity)
                    .collect(Collectors.toList());

            CustomerDetails customerDetails = toCustomerDetailsEntity(request.getCustomerDetails());

            LocalDateTime now = LocalDateTime.now();
            Order order = Order.builder()
                    .orderId(UUID.randomUUID().toString())
                    .items(items)
                    .customerDetails(customerDetails)
                    .status(OrderStatus.PENDING)
                    .orderCreatedAt(now)
                    .orderModifiedAt(now)
                    .build();

            Order savedOrder = orderRepository.save(order);
            return toOrderResponse(savedOrder);
        } catch (Exception ex) {
            throw new InternalServerException("Failed to create order: " + ex.getMessage());
        }
    }

    @Override
    public OrderResponse cancelOrder(String orderId, CancelOrderRequest request) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Order not found with ID: " + orderId));

            if (order.getStatus() != OrderStatus.PENDING) {
                throw new BadRequestException(
                        "Order cannot be cancelled. Only PENDING orders can be cancelled. " +
                        "Current status: " + order.getStatus());
            }

            order.setStatus(OrderStatus.CANCELLED);
            order.setOrderModifiedAt(LocalDateTime.now());

            Order savedOrder = orderRepository.save(order);
            return toOrderResponse(savedOrder);
        } catch (ResourceNotFoundException | BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalServerException("Failed to cancel order: " + ex.getMessage());
        }
    }

    private OrderResponse toOrderResponse(Order order) {
        List<ItemDto> itemDtos = order.getItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .items(itemDtos)
                .customerDetails(toCustomerDetailsDto(order.getCustomerDetails()))
                .status(order.getStatus())
                .orderCreatedAt(order.getOrderCreatedAt())
                .orderModifiedAt(order.getOrderModifiedAt())
                .build();
    }

    private ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }

    private Item toItemEntity(ItemDto dto) {
        return Item.builder()
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .build();
    }

    private CustomerDetailsDto toCustomerDetailsDto(CustomerDetails entity) {
        return CustomerDetailsDto.builder()
                .name(entity.getName())
                .phoneNo(entity.getPhoneNo())
                .address(entity.getAddress())
                .build();
    }

    private CustomerDetails toCustomerDetailsEntity(CustomerDetailsDto dto) {
        return CustomerDetails.builder()
                .name(dto.getName())
                .phoneNo(dto.getPhoneNo())
                .address(dto.getAddress())
                .build();
    }
}
