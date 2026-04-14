package com.ck.PI.orderApi.dto.response;

import com.ck.PI.orderApi.dto.CustomerDetailsDto;
import com.ck.PI.orderApi.dto.ItemDto;
import com.ck.PI.orderApi.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String orderId;
    private List<ItemDto> items;
    private CustomerDetailsDto customerDetails;
    private OrderStatus status;
    private LocalDateTime orderCreatedAt;
    private LocalDateTime orderModifiedAt;
}
