package com.shoestore.modules.architecturefixture.application.service;

import com.shoestore.modules.architecturefixture.application.command.CreateTestProductWithIdCommand;
import com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct;
import com.shoestore.modules.architecturefixture.domain.repository.TestApplicationProductRepository;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.Objects;

public final class CreateTestProductUseCase {

  private final TestApplicationProductRepository productRepository;

  public CreateTestProductUseCase(TestApplicationProductRepository productRepository) {

    this.productRepository =
        Objects.requireNonNull(productRepository, "productRepository must not be null");
  }

  public TestApplicationProductId execute(CreateTestProductWithIdCommand command) {

    Objects.requireNonNull(command, "command must not be null");

    TestApplicationProduct product = new TestApplicationProduct(command.productId(), false);

    TestApplicationProduct savedProduct = productRepository.save(product);

    return savedProduct.id();
  }
}
