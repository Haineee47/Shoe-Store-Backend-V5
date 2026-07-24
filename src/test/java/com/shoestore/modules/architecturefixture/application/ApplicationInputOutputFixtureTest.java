package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.command.CreateTestProductWithIdCommand;
import com.shoestore.modules.architecturefixture.application.command.DeactivateTestProductCommand;
import com.shoestore.modules.architecturefixture.application.dto.ActivateTestProductResult;
import com.shoestore.modules.architecturefixture.application.dto.TestProductDetails;
import com.shoestore.modules.architecturefixture.application.service.CreateTestProductUseCase;
import com.shoestore.modules.architecturefixture.application.service.DeactivateTestProductUseCase;
import com.shoestore.modules.architecturefixture.application.support.InMemoryTestProductRepository;
import com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ApplicationInputOutputFixtureTest {

  @Test
  void createUseCaseShouldReturnTypedIdentifier() {

    TestApplicationProductId productId = new TestApplicationProductId(UUID.randomUUID());

    InMemoryTestProductRepository repository = new InMemoryTestProductRepository();

    CreateTestProductUseCase useCase = new CreateTestProductUseCase(repository);

    TestApplicationProductId result =
        useCase.execute(new CreateTestProductWithIdCommand(productId));

    assertThat(result).isEqualTo(productId);

    assertThat(repository.findById(productId)).isPresent();
  }

  @Test
  void stateChangingUseCaseMayReturnVoid() {

    TestApplicationProductId productId = new TestApplicationProductId(UUID.randomUUID());

    TestApplicationProduct product = new TestApplicationProduct(productId, true);

    InMemoryTestProductRepository repository = new InMemoryTestProductRepository();

    repository.save(product);

    DeactivateTestProductUseCase useCase = new DeactivateTestProductUseCase(repository);

    useCase.execute(new DeactivateTestProductCommand(productId));

    assertThat(repository.findById(productId).orElseThrow().isActive()).isFalse();
  }

  @Test
  void applicationResultShouldNotBeAggregate() {

    assertThat(TestApplicationProduct.class.isAssignableFrom(ActivateTestProductResult.class))
        .isFalse();

    assertThat(TestApplicationProduct.class.isAssignableFrom(TestProductDetails.class)).isFalse();
  }
}
