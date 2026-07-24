package com.shoestore.modules.architecturefixture.domain.repository;

import com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.Optional;

public interface TestApplicationProductRepository {

  Optional<TestApplicationProduct> findById(
      TestApplicationProductId productId);

  TestApplicationProduct save(
      TestApplicationProduct product);
}
