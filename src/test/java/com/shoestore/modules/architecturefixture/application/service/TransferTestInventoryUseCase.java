package com.shoestore.modules.architecturefixture.application.service;

import com.shoestore.modules.architecturefixture.application.command.TransferTestInventoryCommand;
import com.shoestore.modules.architecturefixture.application.dto.TransferTestInventoryResult;
import com.shoestore.modules.architecturefixture.application.port.InventoryTransferNotificationPort;
import com.shoestore.modules.architecturefixture.application.transaction.AfterCommitActions;
import com.shoestore.modules.architecturefixture.application.transaction.ApplicationTransactionOperations;
import com.shoestore.modules.architecturefixture.domain.event.TestInventoryTransferred;
import com.shoestore.modules.architecturefixture.domain.model.TestInventoryAggregate;
import com.shoestore.modules.architecturefixture.domain.repository.TestInventoryRepository;
import java.util.List;
import java.util.Objects;

public final class TransferTestInventoryUseCase {

  private final TestInventoryRepository inventoryRepository;
  private final ApplicationTransactionOperations transactionOperations;
  private final AfterCommitActions afterCommitActions;
  private final InventoryTransferNotificationPort notificationPort;

  public TransferTestInventoryUseCase(
      TestInventoryRepository inventoryRepository,
      ApplicationTransactionOperations transactionOperations,
      AfterCommitActions afterCommitActions,
      InventoryTransferNotificationPort notificationPort) {

    this.inventoryRepository =
        Objects.requireNonNull(inventoryRepository, "inventoryRepository must not be null");

    this.transactionOperations =
        Objects.requireNonNull(transactionOperations, "transactionOperations must not be null");

    this.afterCommitActions =
        Objects.requireNonNull(afterCommitActions, "afterCommitActions must not be null");

    this.notificationPort =
        Objects.requireNonNull(notificationPort, "notificationPort must not be null");
  }

  public TransferTestInventoryResult execute(TransferTestInventoryCommand command) {

    Objects.requireNonNull(command, "command must not be null");

    return transactionOperations.execute(() -> executeTransaction(command));
  }

  private TransferTestInventoryResult executeTransaction(TransferTestInventoryCommand command) {

    TestInventoryAggregate source =
        inventoryRepository
            .findById(command.sourceInventoryId())
            .orElseThrow(() -> new IllegalArgumentException("source inventory was not found"));

    TestInventoryAggregate destination =
        inventoryRepository
            .findById(command.destinationInventoryId())
            .orElseThrow(() -> new IllegalArgumentException("destination inventory was not found"));

    source.remove(command.quantity(), destination.id());

    destination.add(command.quantity());

    TestInventoryAggregate savedSource = inventoryRepository.save(source);

    TestInventoryAggregate savedDestination = inventoryRepository.save(destination);

    List<Object> events = source.pullDomainEvents();

    TestInventoryTransferred transferEvent =
        events.stream()
            .filter(TestInventoryTransferred.class::isInstance)
            .map(TestInventoryTransferred.class::cast)
            .findFirst()
            .orElseThrow(
                () -> new IllegalStateException("inventory transfer event was not recorded"));

    afterCommitActions.register(() -> notificationPort.notifyTransferCompleted(transferEvent));

    return new TransferTestInventoryResult(
        savedSource.id(),
        savedDestination.id(),
        command.quantity(),
        savedSource.availableQuantity(),
        savedDestination.availableQuantity());
  }
}
