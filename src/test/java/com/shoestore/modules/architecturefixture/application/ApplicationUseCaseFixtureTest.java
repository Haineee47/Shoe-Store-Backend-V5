package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.modules.architecturefixture.application.command.ActivateTestProductCommand;
import com.shoestore.modules.architecturefixture.application.dto.ActivateTestProductResult;
import com.shoestore.modules.architecturefixture.application.service.ActivateTestProductUseCase;
import com.shoestore.modules.architecturefixture.application.support.InMemoryTestProductRepository;
import com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ApplicationUseCaseFixtureTest {

  @Test
  void shouldLoadAggregateInvokeDomainBehaviorSaveAndReturnResult() {

    TestApplicationProductId productId =
        new TestApplicationProductId(UUID.randomUUID());

    TestApplicationProduct product =
        new TestApplicationProduct(productId, false);

    InMemoryTestProductRepository repository =
        new InMemoryTestProductRepository();

    repository.save(product);

    ActivateTestProductUseCase useCase =
        new ActivateTestProductUseCase(repository);

    ActivateTestProductResult result =
        useCase.execute(
            new ActivateTestProductCommand(productId));

    assertThat(result.productId()).isEqualTo(productId);
    assertThat(result.active()).isTrue();

    assertThat(repository.findById(productId))
        .isPresent()
        .get()
        .extracting(TestApplicationProduct::isActive)
        .isEqualTo(true);
  }

  @Test
  void shouldRejectNullRepositoryDependency() {

    assertThatThrownBy(
            () -> new ActivateTestProductUseCase(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("productRepository must not be null");
  }

  @Test
  void shouldRejectNullCommand() {

    ActivateTestProductUseCase useCase =
        new ActivateTestProductUseCase(
            new InMemoryTestProductRepository());

    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("command must not be null");
  }

  @Test
  void shouldRejectMissingAggregate() {

    ActivateTestProductUseCase useCase =
        new ActivateTestProductUseCase(
            new InMemoryTestProductRepository());

    TestApplicationProductId productId =
        new TestApplicationProductId(UUID.randomUUID());

    assertThatThrownBy(
            () ->
                useCase.execute(
                    new ActivateTestProductCommand(productId)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("product was not found");
  }
}
