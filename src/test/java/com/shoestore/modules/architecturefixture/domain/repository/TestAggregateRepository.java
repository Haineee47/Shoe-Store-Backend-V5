package com.shoestore.modules.architecturefixture.domain.repository;

import com.shoestore.modules.architecturefixture.domain.model.TestRepositoryAggregate;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryAggregateId;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryLookupKey;
import java.util.Optional;

/**
 * Test-only domain repository port.
 *
 * <p>This contract is defined around an aggregate root and uses domain types exclusively. It must
 * remain independent of Spring Data, JPA and persistence entities.
 */
public interface TestAggregateRepository {

  Optional<TestRepositoryAggregate> findById(TestRepositoryAggregateId aggregateId);

  Optional<TestRepositoryAggregate> findByLookupKey(TestRepositoryLookupKey lookupKey);

  boolean existsByLookupKey(TestRepositoryLookupKey lookupKey);

  TestRepositoryAggregate save(TestRepositoryAggregate aggregate);
}
