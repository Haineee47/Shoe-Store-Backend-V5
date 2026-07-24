package com.shoestore.modules.architecturefixture.domain.repository;

import com.shoestore.modules.architecturefixture.domain.model.TestOrderAggregate;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestOrderId;
import java.util.Optional;

public interface TestOrderRepository {

  Optional<TestOrderAggregate> findById(TestOrderId orderId);

  TestOrderAggregate save(TestOrderAggregate order);
}
