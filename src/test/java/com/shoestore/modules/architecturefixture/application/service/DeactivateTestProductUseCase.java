package com.shoestore.modules.architecturefixture.application.service;

import com.shoestore.modules.architecturefixture.application.command.DeactivateTestProductCommand;
import com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct;
import com.shoestore.modules.architecturefixture.domain.repository.TestApplicationProductRepository;
import java.util.Objects;

public final class DeactivateTestProductUseCase {

  private final TestApplicationProductRepository productRepository;

  public DeactivateTestProductUseCase(TestApplicationProductRepository productRepository) {

    this.productRepository =
        Objects.requireNonNull(productRepository, "productRepository must not be null");
  }

  public void execute(DeactivateTestProductCommand command) {

    Objects.requireNonNull(command, "command must not be null");

    TestApplicationProduct product =
        productRepository
            .findById(command.productId())
            .orElseThrow(() -> new IllegalArgumentException("product was not found"));

    product.deactivate();

    productRepository.save(product);
  }
}
