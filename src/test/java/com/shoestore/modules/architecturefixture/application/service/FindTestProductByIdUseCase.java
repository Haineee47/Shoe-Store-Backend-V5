package com.shoestore.modules.architecturefixture.application.service;

import com.shoestore.modules.architecturefixture.application.dto.TestProductDetails;
import com.shoestore.modules.architecturefixture.application.query.FindTestProductByIdQuery;
import com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct;
import com.shoestore.modules.architecturefixture.domain.repository.TestApplicationProductRepository;
import java.util.Objects;

public final class FindTestProductByIdUseCase {

  private final TestApplicationProductRepository productRepository;

  public FindTestProductByIdUseCase(TestApplicationProductRepository productRepository) {

    this.productRepository =
        Objects.requireNonNull(productRepository, "productRepository must not be null");
  }

  public TestProductDetails execute(FindTestProductByIdQuery query) {

    Objects.requireNonNull(query, "query must not be null");

    TestApplicationProduct product =
        productRepository
            .findById(query.productId())
            .orElseThrow(() -> new IllegalArgumentException("product was not found"));

    return new TestProductDetails(product.id(), product.isActive());
  }
}
