package com.shoestore.modules.architecturefixture.application.transaction;

import java.util.function.Supplier;

public interface ApplicationTransactionOperations {

  <T> T execute(Supplier<T> transactionalWork);
}
