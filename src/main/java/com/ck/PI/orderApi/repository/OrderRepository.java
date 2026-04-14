package com.ck.PI.orderApi.repository;

import com.ck.PI.orderApi.entity.Order;
import com.ck.PI.orderApi.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
