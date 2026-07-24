package com.shoestore.modules.architecturefixture.application.service;

import com.shoestore.modules.architecturefixture.application.command.ActivateTestProductCommand;
import com.shoestore.modules.architecturefixture.application.dto.ActivateTestProductResult;
import com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct;
import com.shoestore.modules.architecturefixture.domain.repository.TestApplicationProductRepository;
import java.util.Objects;

public final class ActivateTestProductUseCase {

  private final TestApplicationProductRepository productRepository;

  public ActivateTestProductUseCase(
      TestApplicationProductRepository productRepository) {

    this.productRepository =
        Objects.requireNonNull(
            productRepository,
            "productRepository must not be null");
  }

  public ActivateTestProductResult execute(
      ActivateTestProductCommand command) {

    Objects.requireNonNull(command, "command must not be null");

    TestApplicationProduct product =
        productRepository
            .findById(command.productId())
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "product was not found"));

    product.activate();

    TestApplicationProduct savedProduct =
        productRepository.save(product);

    return new ActivateTestProductResult(
        savedProduct.id(),
        savedProduct.isActive());
  }
}
