package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.modules.architecturefixture.application.dto.TestProductDetails;
import com.shoestore.modules.architecturefixture.application.query.FindTestProductByIdQuery;
import com.shoestore.modules.architecturefixture.application.service.FindTestProductByIdUseCase;
import com.shoestore.modules.architecturefixture.application.support.InMemoryTestProductRepository;
import com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ApplicationQueryFixtureTest {

  @Test
  void shouldReturnApplicationReadResultForExistingProduct() {

    TestApplicationProductId productId = new TestApplicationProductId(UUID.randomUUID());

    InMemoryTestProductRepository repository = new InMemoryTestProductRepository();

    repository.save(new TestApplicationProduct(productId, true));

    FindTestProductByIdUseCase useCase = new FindTestProductByIdUseCase(repository);

    TestProductDetails result = useCase.execute(new FindTestProductByIdQuery(productId));

    assertThat(result.productId()).isEqualTo(productId);

    assertThat(result.active()).isTrue();
  }

  @Test
  void shouldRejectNullRepositoryDependency() {

    assertThatThrownBy(() -> new FindTestProductByIdUseCase(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("productRepository must not be null");
  }

  @Test
  void shouldRejectNullQuery() {

    FindTestProductByIdUseCase useCase =
        new FindTestProductByIdUseCase(new InMemoryTestProductRepository());

    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("query must not be null");
  }

  @Test
  void shouldRejectMissingProduct() {

    TestApplicationProductId productId = new TestApplicationProductId(UUID.randomUUID());

    FindTestProductByIdUseCase useCase =
        new FindTestProductByIdUseCase(new InMemoryTestProductRepository());

    assertThatThrownBy(() -> useCase.execute(new FindTestProductByIdQuery(productId)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("product was not found");
  }
}
