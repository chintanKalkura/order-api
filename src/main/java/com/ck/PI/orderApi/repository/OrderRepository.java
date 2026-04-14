package com.ck.PI.orderApi.repository;

import com.ck.PI.orderApi.entity.Order;
import com.ck.PI.orderApi.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByStatus(OrderStatus status);
}
