package com.ck.PI.orderApi.dto.request;

import com.ck.PI.orderApi.dto.CustomerDetailsDto;
import com.ck.PI.orderApi.dto.ItemDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "Items list must not be null")
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<ItemDto> items;

    @NotNull(message = "Customer details must not be null")
    @Valid
    private CustomerDetailsDto customerDetails;
}
