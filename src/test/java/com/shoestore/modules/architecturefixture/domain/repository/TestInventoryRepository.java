package com.shoestore.modules.architecturefixture.domain.repository;

import com.shoestore.modules.architecturefixture.domain.model.TestInventoryAggregate;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestInventoryId;
import java.util.Optional;

public interface TestInventoryRepository {

  Optional<TestInventoryAggregate> findById(TestInventoryId inventoryId);

  TestInventoryAggregate save(TestInventoryAggregate inventory);
}
