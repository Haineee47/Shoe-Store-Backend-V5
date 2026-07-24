package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.service.TransferTestInventoryUseCase;
import com.shoestore.modules.architecturefixture.application.transaction.AfterCommitActions;
import com.shoestore.modules.architecturefixture.application.transaction.ApplicationTransactionOperations;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class ApplicationTransactionStructureConventionTest {

  @Test
  void transactionContractsShouldBeInterfaces() {

    assertThat(ApplicationTransactionOperations.class.isInterface()).isTrue();

    assertThat(AfterCommitActions.class.isInterface()).isTrue();
  }

  @Test
  void transactionContractsShouldBelongToApplicationLayer() {

    assertThat(ApplicationTransactionOperations.class.getPackageName())
        .contains(".application.transaction");

    assertThat(AfterCommitActions.class.getPackageName()).contains(".application.transaction");
  }

  @Test
  void transactionalUseCaseShouldRemainAConcreteFinalClass() {

    assertThat(Modifier.isFinal(TransferTestInventoryUseCase.class.getModifiers())).isTrue();
  }
}
