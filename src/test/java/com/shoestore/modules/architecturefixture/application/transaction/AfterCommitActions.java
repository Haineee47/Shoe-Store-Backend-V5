package com.shoestore.modules.architecturefixture.application.transaction;

public interface AfterCommitActions {

  void register(Runnable action);
}
