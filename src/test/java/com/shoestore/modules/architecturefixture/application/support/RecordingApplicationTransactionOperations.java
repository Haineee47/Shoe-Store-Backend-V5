package com.shoestore.modules.architecturefixture.application.support;

import com.shoestore.modules.architecturefixture.application.transaction.AfterCommitActions;
import com.shoestore.modules.architecturefixture.application.transaction.ApplicationTransactionOperations;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class RecordingApplicationTransactionOperations
    implements ApplicationTransactionOperations, AfterCommitActions {

  private final Runnable beginAction;
  private final Runnable commitAction;
  private final Runnable rollbackAction;
  private final List<Runnable> afterCommitActions = new ArrayList<>();

  private boolean transactionActive;
  private int beginCount;
  private int commitCount;
  private int rollbackCount;

  public RecordingApplicationTransactionOperations(
      Runnable beginAction, Runnable commitAction, Runnable rollbackAction) {

    this.beginAction = Objects.requireNonNull(beginAction, "beginAction must not be null");

    this.commitAction = Objects.requireNonNull(commitAction, "commitAction must not be null");

    this.rollbackAction = Objects.requireNonNull(rollbackAction, "rollbackAction must not be null");
  }

  @Override
  public <T> T execute(Supplier<T> transactionalWork) {

    Objects.requireNonNull(transactionalWork, "transactionalWork must not be null");

    if (transactionActive) {
      throw new IllegalStateException("nested fixture transaction is not supported");
    }

    transactionActive = true;
    beginCount++;
    beginAction.run();

    try {
      T result = transactionalWork.get();

      commitAction.run();
      commitCount++;
      transactionActive = false;

      List<Runnable> actions = List.copyOf(afterCommitActions);

      afterCommitActions.clear();
      actions.forEach(Runnable::run);

      return result;
    } catch (RuntimeException exception) {

      rollbackAction.run();
      rollbackCount++;
      transactionActive = false;
      afterCommitActions.clear();

      throw exception;
    }
  }

  @Override
  public void register(Runnable action) {

    Objects.requireNonNull(action, "action must not be null");

    if (!transactionActive) {
      throw new IllegalStateException("after-commit action requires an active transaction");
    }

    afterCommitActions.add(action);
  }

  public boolean isTransactionActive() {
    return transactionActive;
  }

  public int beginCount() {
    return beginCount;
  }

  public int commitCount() {
    return commitCount;
  }

  public int rollbackCount() {
    return rollbackCount;
  }
}
