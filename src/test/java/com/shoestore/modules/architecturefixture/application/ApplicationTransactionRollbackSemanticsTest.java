package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.modules.architecturefixture.application.command.TransferTestInventoryCommand;
import com.shoestore.modules.architecturefixture.application.service.TransferTestInventoryUseCase;
import com.shoestore.modules.architecturefixture.application.support.RecordingApplicationTransactionOperations;
import com.shoestore.modules.architecturefixture.application.support.RecordingInventoryTransferNotification;
import com.shoestore.modules.architecturefixture.application.support.TrackingTestInventoryRepository;
import com.shoestore.modules.architecturefixture.domain.model.TestInventoryAggregate;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestInventoryId;
import org.junit.jupiter.api.Test;

class ApplicationTransactionRollbackSemanticsTest {

  @Test
  void shouldRollbackAllRepositoryChangesWhenSecondSaveFails() {

    TestInventoryId sourceId = TestInventoryId.generate();

    TestInventoryId destinationId = TestInventoryId.generate();

    TrackingTestInventoryRepository repository = new TrackingTestInventoryRepository();

    repository.seed(new TestInventoryAggregate(sourceId, 10));

    repository.seed(new TestInventoryAggregate(destinationId, 2));

    repository.failOnSaveCall(2);

    RecordingApplicationTransactionOperations transaction =
        new RecordingApplicationTransactionOperations(
            repository::beginTransaction,
            repository::commitTransaction,
            repository::rollbackTransaction);

    RecordingInventoryTransferNotification notification =
        new RecordingInventoryTransferNotification();

    TransferTestInventoryUseCase useCase =
        new TransferTestInventoryUseCase(repository, transaction, transaction, notification);

    assertThatThrownBy(
            () -> useCase.execute(new TransferTestInventoryCommand(sourceId, destinationId, 4)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("simulated repository failure");

    assertThat(repository.findById(sourceId).orElseThrow().availableQuantity()).isEqualTo(10);

    assertThat(repository.findById(destinationId).orElseThrow().availableQuantity()).isEqualTo(2);

    assertThat(transaction.commitCount()).isZero();

    assertThat(transaction.rollbackCount()).isEqualTo(1);

    assertThat(notification.callCount()).isZero();
  }
}
