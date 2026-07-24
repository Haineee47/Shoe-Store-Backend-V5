package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.query.FindTestProductByIdQuery;
import com.shoestore.modules.architecturefixture.application.service.FindTestProductByIdUseCase;
import com.shoestore.modules.architecturefixture.application.support.TrackingTestProductRepository;
import com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ApplicationQueryReadOnlySemanticsTest {

  @Test
  void queryUseCaseShouldLoadWithoutSavingAggregate() {

    TestApplicationProductId productId = new TestApplicationProductId(UUID.randomUUID());

    TestApplicationProduct product = new TestApplicationProduct(productId, false);

    TrackingTestProductRepository repository = new TrackingTestProductRepository();

    repository.seed(product);

    FindTestProductByIdUseCase useCase = new FindTestProductByIdUseCase(repository);

    useCase.execute(new FindTestProductByIdQuery(productId));

    assertThat(repository.findByIdCallCount()).isEqualTo(1);

    assertThat(repository.saveCallCount()).isZero();

    assertThat(product.isActive()).isFalse();
  }
}
