package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.modules.architecturefixture.application.command.CompleteTestOrderCommand;
import com.shoestore.modules.architecturefixture.application.dto.CompleteTestOrderResult;
import com.shoestore.modules.architecturefixture.application.service.CompleteTestOrderUseCase;
import com.shoestore.modules.architecturefixture.application.support.RecordingOrderCompletionNotification;
import com.shoestore.modules.architecturefixture.application.support.TrackingTestOrderRepository;
import com.shoestore.modules.architecturefixture.domain.model.TestOrderAggregate;
import com.shoestore.modules.architecturefixture.domain.model.internal.TestOrderLine;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestOrderId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationServiceFixtureTest {

  @Test
  void shouldCompleteOrderThroughAggregateRoot() {

    List<String> operations = new ArrayList<>();

    TestOrderId orderId = TestOrderId.generate();

    TestOrderAggregate order =
        new TestOrderAggregate(
            orderId, List.of(new TestOrderLine("SHOE-001", 1), new TestOrderLine("SHOE-002", 2)));

    TrackingTestOrderRepository repository = new TrackingTestOrderRepository(operations);

    repository.seed(order);

    RecordingOrderCompletionNotification notification =
        new RecordingOrderCompletionNotification(operations);

    CompleteTestOrderUseCase useCase = new CompleteTestOrderUseCase(repository, notification);

    CompleteTestOrderResult result = useCase.execute(new CompleteTestOrderCommand(orderId));

    assertThat(result.orderId()).isEqualTo(orderId);

    assertThat(result.completed()).isTrue();

    assertThat(result.fulfilledLineCount()).isEqualTo(2);

    assertThat(order.isCompleted()).isTrue();

    assertThat(order.areAllLinesFulfilled()).isTrue();

    assertThat(notification.callCount()).isEqualTo(1);

    assertThat(notification.notifiedOrderId()).isEqualTo(orderId);
  }

  @Test
  void shouldRejectNullRepository() {

    assertThatThrownBy(() -> new CompleteTestOrderUseCase(null, orderId -> {}))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("orderRepository must not be null");
  }

  @Test
  void shouldRejectNullNotificationPort() {

    TrackingTestOrderRepository repository = new TrackingTestOrderRepository(new ArrayList<>());

    assertThatThrownBy(() -> new CompleteTestOrderUseCase(repository, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("notificationPort must not be null");
  }

  @Test
  void shouldRejectNullCommand() {

    CompleteTestOrderUseCase useCase =
        new CompleteTestOrderUseCase(
            new TrackingTestOrderRepository(new ArrayList<>()), orderId -> {});

    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("command must not be null");
  }

  @Test
  void shouldRejectMissingAggregate() {

    CompleteTestOrderUseCase useCase =
        new CompleteTestOrderUseCase(
            new TrackingTestOrderRepository(new ArrayList<>()), orderId -> {});

    assertThatThrownBy(() -> useCase.execute(new CompleteTestOrderCommand(TestOrderId.generate())))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("order was not found");
  }
}
