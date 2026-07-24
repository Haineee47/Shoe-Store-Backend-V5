package com.shoestore.modules.architecturefixture.domain.repository.support;

import com.shoestore.modules.architecturefixture.domain.model.TestRepositoryAggregate;
import com.shoestore.modules.architecturefixture.domain.repository.TestAggregateRepository;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryAggregateId;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryLookupKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Test-only in-memory implementation of the aggregate repository fixture.
 *
 * <p>This implementation exists only to verify the behavior and usability of the Domain Repository
 * contract. It is not a production repository and does not define persistence adapter conventions.
 */
public final class InMemoryTestAggregateRepository implements TestAggregateRepository {

  private final Map<TestRepositoryAggregateId, TestRepositoryAggregate> aggregatesById =
      new LinkedHashMap<>();

  @Override
  public Optional<TestRepositoryAggregate> findById(
      TestRepositoryAggregateId aggregateId) {

    Objects.requireNonNull(aggregateId, "aggregateId must not be null");

    return Optional.ofNullable(aggregatesById.get(aggregateId));
  }

  @Override
  public Optional<TestRepositoryAggregate> findByLookupKey(
      TestRepositoryLookupKey lookupKey) {

    Objects.requireNonNull(lookupKey, "lookupKey must not be null");

    return aggregatesById.values().stream()
        .filter(aggregate -> aggregate.lookupKey().equals(lookupKey))
        .findFirst();
  }

  @Override
  public boolean existsByLookupKey(TestRepositoryLookupKey lookupKey) {
    Objects.requireNonNull(lookupKey, "lookupKey must not be null");

    return aggregatesById.values().stream()
        .anyMatch(aggregate -> aggregate.lookupKey().equals(lookupKey));
  }

  @Override
  public TestRepositoryAggregate save(TestRepositoryAggregate aggregate) {
    Objects.requireNonNull(aggregate, "aggregate must not be null");

    aggregatesById.put(aggregate.id(), aggregate);

    return aggregate;
  }
}
