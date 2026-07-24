package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.command.CompleteTestOrderCommand;
import com.shoestore.modules.architecturefixture.application.service.CompleteTestOrderUseCase;
import com.shoestore.modules.architecturefixture.application.support.RecordingOrderCompletionNotification;
import com.shoestore.modules.architecturefixture.application.support.TrackingTestOrderRepository;
import com.shoestore.modules.architecturefixture.domain.model.TestOrderAggregate;
import com.shoestore.modules.architecturefixture.domain.model.internal.TestOrderLine;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestOrderId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationServiceOrchestrationConventionTest {

  @Test
  void shouldLoadMutateSaveNotifyInRequiredOrder() {

    List<String> operations = new ArrayList<>();

    TestOrderId orderId = TestOrderId.generate();

    TestOrderAggregate order =
        new TestOrderAggregate(orderId, List.of(new TestOrderLine("SHOE-001", 1)));

    TrackingTestOrderRepository repository = new TrackingTestOrderRepository(operations);

    repository.seed(order);

    CompleteTestOrderUseCase useCase =
        new CompleteTestOrderUseCase(
            repository, new RecordingOrderCompletionNotification(operations));

    useCase.execute(new CompleteTestOrderCommand(orderId));

    assertThat(operations).containsExactly("load", "save", "notify");

    assertThat(order.isCompleted()).isTrue();

    assertThat(order.areAllLinesFulfilled()).isTrue();
  }

  @Test
  void domainBehaviorShouldOccurBeforeRepositorySave() {

    List<String> operations = new ArrayList<>();

    TestOrderId orderId = TestOrderId.generate();

    TestOrderAggregate order =
        new TestOrderAggregate(orderId, List.of(new TestOrderLine("SHOE-001", 1)));

    TrackingTestOrderRepository repository = new TrackingTestOrderRepository(operations);

    repository.seed(order);

    new CompleteTestOrderUseCase(repository, new RecordingOrderCompletionNotification(operations))
        .execute(new CompleteTestOrderCommand(orderId));

    assertThat(order.isCompleted()).isTrue();

    assertThat(order.areAllLinesFulfilled()).isTrue();
  }
}
