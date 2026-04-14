package com.ck.PI.orderApi.entity;

import com.ck.PI.orderApi.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private String orderId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "order_items",
            joinColumns = @JoinColumn(name = "order_id")
    )
    private List<Item> items;

    @Embedded
    private CustomerDetails customerDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "order_created_at", nullable = false, updatable = false)
    private LocalDateTime orderCreatedAt;

    @Column(name = "order_modified_at", nullable = false)
    private LocalDateTime orderModifiedAt;
}
