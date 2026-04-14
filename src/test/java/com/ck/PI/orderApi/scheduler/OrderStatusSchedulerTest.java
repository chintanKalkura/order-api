package com.ck.PI.orderApi.scheduler;

import com.ck.PI.orderApi.enums.OrderStatus;
import com.ck.PI.orderApi.exception.BulkOrderUpdateException;
import com.ck.PI.orderApi.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderStatusSchedulerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderStatusScheduler scheduler;

    @Test
    void processPendingOrders_pendingOrdersExist_bulkUpdatesToProcessing() throws BulkOrderUpdateException {
        when(orderRepository.bulkUpdateStatus(
                eq(OrderStatus.PENDING),
                eq(OrderStatus.PROCESSING),
                any(LocalDateTime.class)
        )).thenReturn(3);

        scheduler.processPendingOrders();

        verify(orderRepository).bulkUpdateStatus(
                eq(OrderStatus.PENDING),
                eq(OrderStatus.PROCESSING),
                any(LocalDateTime.class)
        );
    }

    @Test
    void processPendingOrders_noPendingOrders_callsRepositoryWithZeroResult() throws BulkOrderUpdateException {
        when(orderRepository.bulkUpdateStatus(
                eq(OrderStatus.PENDING),
                eq(OrderStatus.PROCESSING),
                any(LocalDateTime.class)
        )).thenReturn(0);

        scheduler.processPendingOrders();

        verify(orderRepository).bulkUpdateStatus(
                eq(OrderStatus.PENDING),
                eq(OrderStatus.PROCESSING),
                any(LocalDateTime.class)
        );
    }

    @Test
    void processPendingOrders_repositoryThrows_exceptionIsPropagated() {
        when(orderRepository.bulkUpdateStatus(any(), any(), any()))
                .thenThrow(new RuntimeException("DB connection lost"));

        assertThrows(BulkOrderUpdateException.class, () -> scheduler.processPendingOrders());
    }
}
