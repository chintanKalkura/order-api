package com.ck.PI.orderApi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailsDto {

    @NotBlank(message = "Customer name must not be blank")
    private String name;

    @NotBlank(message = "Phone number must not be blank")
    private String phoneNo;

    @NotBlank(message = "Address must not be blank")
    private String address;
}
