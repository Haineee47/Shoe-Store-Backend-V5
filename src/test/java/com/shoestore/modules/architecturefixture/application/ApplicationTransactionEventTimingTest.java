package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.command.TransferTestInventoryCommand;
import com.shoestore.modules.architecturefixture.application.service.TransferTestInventoryUseCase;
import com.shoestore.modules.architecturefixture.application.support.RecordingApplicationTransactionOperations;
import com.shoestore.modules.architecturefixture.application.support.TrackingTestInventoryRepository;
import com.shoestore.modules.architecturefixture.domain.event.TestInventoryTransferred;
import com.shoestore.modules.architecturefixture.domain.model.TestInventoryAggregate;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestInventoryId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationTransactionEventTimingTest {

  @Test
  void externalNotificationShouldRunOnlyAfterCommit() {

    List<String> operations = new ArrayList<>();

    TestInventoryId sourceId = TestInventoryId.generate();

    TestInventoryId destinationId = TestInventoryId.generate();

    TrackingTestInventoryRepository repository = new TrackingTestInventoryRepository();

    repository.seed(new TestInventoryAggregate(sourceId, 10));

    repository.seed(new TestInventoryAggregate(destinationId, 0));

    RecordingApplicationTransactionOperations transaction =
        new RecordingApplicationTransactionOperations(
            () -> {
              operations.add("begin");
              repository.beginTransaction();
            },
            () -> {
              operations.add("commit");
              repository.commitTransaction();
            },
            () -> {
              operations.add("rollback");
              repository.rollbackTransaction();
            });

    TransferTestInventoryUseCase useCase =
        new TransferTestInventoryUseCase(
            repository,
            transaction,
            transaction,
            event -> operations.add(notificationOperation(event)));

    useCase.execute(new TransferTestInventoryCommand(sourceId, destinationId, 3));

    assertThat(operations).containsExactly("begin", "commit", "notify");
  }

  private static String notificationOperation(TestInventoryTransferred event) {

    return event == null ? "invalid-notification" : "notify";
  }
}
