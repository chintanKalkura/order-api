package com.ck.PI.orderApi.repository;

import com.ck.PI.orderApi.entity.Order;
import com.ck.PI.orderApi.enums.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :newStatus, o.orderModifiedAt = :modifiedAt WHERE o.status = :currentStatus")
    int bulkUpdateStatus(
            @Param("currentStatus") OrderStatus currentStatus,
            @Param("newStatus") OrderStatus newStatus,
            @Param("modifiedAt") LocalDateTime modifiedAt);
}
