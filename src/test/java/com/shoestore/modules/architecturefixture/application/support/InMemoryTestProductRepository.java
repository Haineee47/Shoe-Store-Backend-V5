package com.shoestore.modules.architecturefixture.application.support;

import com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct;
import com.shoestore.modules.architecturefixture.domain.repository.TestApplicationProductRepository;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryTestProductRepository
    implements TestApplicationProductRepository {

  private final Map<TestApplicationProductId, TestApplicationProduct> storage =
      new LinkedHashMap<>();

  @Override
  public Optional<TestApplicationProduct> findById(
      TestApplicationProductId productId) {

    Objects.requireNonNull(productId, "productId must not be null");

    return Optional.ofNullable(storage.get(productId));
  }

  @Override
  public TestApplicationProduct save(
      TestApplicationProduct product) {

    Objects.requireNonNull(product, "product must not be null");

    storage.put(product.id(), product);
    return product;
  }
}
