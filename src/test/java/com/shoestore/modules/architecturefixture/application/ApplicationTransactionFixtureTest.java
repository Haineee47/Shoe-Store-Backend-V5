package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.command.TransferTestInventoryCommand;
import com.shoestore.modules.architecturefixture.application.dto.TransferTestInventoryResult;
import com.shoestore.modules.architecturefixture.application.service.TransferTestInventoryUseCase;
import com.shoestore.modules.architecturefixture.application.support.RecordingApplicationTransactionOperations;
import com.shoestore.modules.architecturefixture.application.support.RecordingInventoryTransferNotification;
import com.shoestore.modules.architecturefixture.application.support.TrackingTestInventoryRepository;
import com.shoestore.modules.architecturefixture.domain.model.TestInventoryAggregate;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestInventoryId;
import org.junit.jupiter.api.Test;

class ApplicationTransactionFixtureTest {

  @Test
  void shouldCoordinateMultipleRepositoryChangesInOneTransaction() {

    TestInventoryId sourceId = TestInventoryId.generate();

    TestInventoryId destinationId = TestInventoryId.generate();

    TrackingTestInventoryRepository repository = new TrackingTestInventoryRepository();

    repository.seed(new TestInventoryAggregate(sourceId, 10));

    repository.seed(new TestInventoryAggregate(destinationId, 2));

    RecordingApplicationTransactionOperations transaction =
        new RecordingApplicationTransactionOperations(
            repository::beginTransaction,
            repository::commitTransaction,
            repository::rollbackTransaction);

    RecordingInventoryTransferNotification notification =
        new RecordingInventoryTransferNotification();

    TransferTestInventoryUseCase useCase =
        new TransferTestInventoryUseCase(repository, transaction, transaction, notification);

    TransferTestInventoryResult result =
        useCase.execute(new TransferTestInventoryCommand(sourceId, destinationId, 4));

    assertThat(result.sourceRemainingQuantity()).isEqualTo(6);

    assertThat(result.destinationAvailableQuantity()).isEqualTo(6);

    assertThat(transaction.beginCount()).isEqualTo(1);

    assertThat(transaction.commitCount()).isEqualTo(1);

    assertThat(transaction.rollbackCount()).isZero();

    assertThat(notification.callCount()).isEqualTo(1);
  }
}
