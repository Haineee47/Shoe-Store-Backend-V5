package com.shoestore.modules.architecturefixture.application.service;

import com.shoestore.modules.architecturefixture.application.command.CompleteTestOrderCommand;
import com.shoestore.modules.architecturefixture.application.dto.CompleteTestOrderResult;
import com.shoestore.modules.architecturefixture.application.port.OrderCompletionNotificationPort;
import com.shoestore.modules.architecturefixture.domain.model.TestOrderAggregate;
import com.shoestore.modules.architecturefixture.domain.repository.TestOrderRepository;
import java.util.Objects;

public final class CompleteTestOrderUseCase {

  private final TestOrderRepository orderRepository;
  private final OrderCompletionNotificationPort notificationPort;

  public CompleteTestOrderUseCase(
      TestOrderRepository orderRepository, OrderCompletionNotificationPort notificationPort) {

    this.orderRepository =
        Objects.requireNonNull(orderRepository, "orderRepository must not be null");

    this.notificationPort =
        Objects.requireNonNull(notificationPort, "notificationPort must not be null");
  }

  public CompleteTestOrderResult execute(CompleteTestOrderCommand command) {

    Objects.requireNonNull(command, "command must not be null");

    TestOrderAggregate order =
        orderRepository
            .findById(command.orderId())
            .orElseThrow(() -> new IllegalArgumentException("order was not found"));

    order.complete();

    TestOrderAggregate savedOrder = orderRepository.save(order);

    notificationPort.notifyOrderCompleted(savedOrder.id());

    return new CompleteTestOrderResult(
        savedOrder.id(), savedOrder.isCompleted(), savedOrder.lineCount());
  }
}
