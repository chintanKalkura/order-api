package com.ck.PI.orderApi.scheduler;

import com.ck.PI.orderApi.enums.OrderStatus;
import com.ck.PI.orderApi.exception.BulkOrderUpdateException;
import com.ck.PI.orderApi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final OrderRepository orderRepository;

    @Scheduled(cron = "0 */5 * * * *")
    public void processPendingOrders() throws BulkOrderUpdateException {
        try {
            int updated = orderRepository.bulkUpdateStatus(
                    OrderStatus.PENDING,
                    OrderStatus.PROCESSING,
                    LocalDateTime.now()
            );
        } catch (Exception ex) {
            throw new BulkOrderUpdateException("Exception occurred while bulk updating Orders. Orders moving from status : " + OrderStatus.PENDING.toString() + "to new status : " + OrderStatus.PROCESSING.toString());
        }
    }
}
