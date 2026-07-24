package com.shoestore.modules.architecturefixture.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.shoestore.modules.architecturefixture.domain.model.TestRepositoryAggregate;
import com.shoestore.modules.architecturefixture.domain.repository.support.InMemoryTestAggregateRepository;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryAggregateId;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryLookupKey;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DomainRepositorySaveRetrievalSemanticsTest {

  private InMemoryTestAggregateRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryTestAggregateRepository();
  }

  @Test
  void saveShouldReturnTheSameAggregateInstance() {
    TestRepositoryAggregate aggregate = createAggregate("same-instance@example.com");

    TestRepositoryAggregate saved = repository.save(aggregate);

    assertThat(saved).isSameAs(aggregate);
  }

  @Test
  void savedAggregateShouldBeRetrievableByTypedIdentity() {
    TestRepositoryAggregate aggregate = createAggregate("identity@example.com");

    repository.save(aggregate);

    assertThat(repository.findById(aggregate.id())).containsSame(aggregate);
  }

  @Test
  void savedAggregateShouldBeRetrievableByLookupValue() {
    TestRepositoryAggregate aggregate = createAggregate("lookup@example.com");

    repository.save(aggregate);

    assertThat(repository.findByLookupKey(aggregate.lookupKey())).containsSame(aggregate);
  }

  @Test
  void existsShouldBeTrueForSavedLookupValue() {
    TestRepositoryAggregate aggregate = createAggregate("exists@example.com");

    repository.save(aggregate);

    assertThat(repository.existsByLookupKey(aggregate.lookupKey())).isTrue();
  }

  @Test
  void missingIdentityShouldReturnEmptyOptional() {
    TestRepositoryAggregateId missingId = new TestRepositoryAggregateId(UUID.randomUUID());

    assertThat(repository.findById(missingId)).isEmpty();
  }

  @Test
  void missingLookupValueShouldReturnEmptyOptional() {
    TestRepositoryLookupKey missingLookupKey = new TestRepositoryLookupKey("missing@example.com");

    assertThat(repository.findByLookupKey(missingLookupKey)).isEmpty();
  }

  @Test
  void missingLookupValueShouldNotExist() {
    TestRepositoryLookupKey missingLookupKey = new TestRepositoryLookupKey("missing@example.com");

    assertThat(repository.existsByLookupKey(missingLookupKey)).isFalse();
  }

  @Test
  void savingSameIdentityShouldReplaceStoredAggregate() {
    TestRepositoryAggregateId aggregateId = new TestRepositoryAggregateId(UUID.randomUUID());

    TestRepositoryAggregate original =
        new TestRepositoryAggregate(
            aggregateId, new TestRepositoryLookupKey("original@example.com"));

    TestRepositoryAggregate replacement =
        new TestRepositoryAggregate(
            aggregateId, new TestRepositoryLookupKey("replacement@example.com"));

    repository.save(original);
    repository.save(replacement);

    assertThat(repository.findById(aggregateId)).containsSame(replacement);
  }

  @Test
  void replacingAggregateShouldRemovePreviousLookupSemantics() {
    TestRepositoryAggregateId aggregateId = new TestRepositoryAggregateId(UUID.randomUUID());

    TestRepositoryLookupKey originalLookup = new TestRepositoryLookupKey("original@example.com");

    TestRepositoryLookupKey replacementLookup =
        new TestRepositoryLookupKey("replacement@example.com");

    repository.save(new TestRepositoryAggregate(aggregateId, originalLookup));

    repository.save(new TestRepositoryAggregate(aggregateId, replacementLookup));

    assertThat(repository.findByLookupKey(originalLookup)).isEmpty();

    assertThat(repository.existsByLookupKey(originalLookup)).isFalse();

    assertThat(repository.findByLookupKey(replacementLookup)).isPresent();

    assertThat(repository.existsByLookupKey(replacementLookup)).isTrue();
  }

  @Test
  void savingOneAggregateShouldNotReplaceDifferentIdentity() {
    TestRepositoryAggregate first = createAggregate("first@example.com");

    TestRepositoryAggregate second = createAggregate("second@example.com");

    repository.save(first);
    repository.save(second);

    assertThat(repository.findById(first.id())).containsSame(first);

    assertThat(repository.findById(second.id())).containsSame(second);
  }

  @Test
  void retrievalShouldNotClearPendingDomainEvents() {
    TestRepositoryAggregate aggregate = createAggregate("events@example.com");

    aggregate.recordTestEvent();

    assertThat(aggregate.domainEvents()).hasSize(1);

    repository.save(aggregate);

    TestRepositoryAggregate retrieved = repository.findById(aggregate.id()).orElseThrow();

    assertThat(retrieved.domainEvents()).hasSize(1);
  }

  @Test
  void saveShouldNotClearPendingDomainEvents() {
    TestRepositoryAggregate aggregate = createAggregate("save-events@example.com");

    aggregate.recordTestEvent();

    repository.save(aggregate);

    assertThat(aggregate.domainEvents()).hasSize(1);
  }

  @Test
  void repositoryShouldRejectNullAggregateOnSave() {
    assertThatNullPointerException()
        .isThrownBy(() -> repository.save(null))
        .withMessage("aggregate must not be null");
  }

  @Test
  void repositoryShouldRejectNullIdentityLookup() {
    assertThatNullPointerException()
        .isThrownBy(() -> repository.findById(null))
        .withMessage("aggregateId must not be null");
  }

  @Test
  void repositoryShouldRejectNullDomainLookupValue() {
    assertThatNullPointerException()
        .isThrownBy(() -> repository.findByLookupKey(null))
        .withMessage("lookupKey must not be null");
  }

  @Test
  void repositoryShouldRejectNullExistenceLookupValue() {
    assertThatNullPointerException()
        .isThrownBy(() -> repository.existsByLookupKey(null))
        .withMessage("lookupKey must not be null");
  }

  private TestRepositoryAggregate createAggregate(String lookupValue) {

    return new TestRepositoryAggregate(
        new TestRepositoryAggregateId(UUID.randomUUID()), new TestRepositoryLookupKey(lookupValue));
  }
}
