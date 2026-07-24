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

class DomainRepositoryFixtureTest {

  private InMemoryTestAggregateRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryTestAggregateRepository();
  }

  @Test
  void shouldSaveAndFindAggregateByTypedIdentity() {
    TestRepositoryAggregateId aggregateId = new TestRepositoryAggregateId(UUID.randomUUID());

    TestRepositoryAggregate aggregate =
        new TestRepositoryAggregate(
            aggregateId, new TestRepositoryLookupKey("fixture@example.com"));

    TestRepositoryAggregate savedAggregate = repository.save(aggregate);

    assertThat(savedAggregate).isSameAs(aggregate);
    assertThat(repository.findById(aggregateId)).containsSame(aggregate);
  }

  @Test
  void shouldFindAggregateByDomainLookupValue() {
    TestRepositoryLookupKey lookupKey = new TestRepositoryLookupKey("fixture@example.com");

    TestRepositoryAggregate aggregate =
        new TestRepositoryAggregate(new TestRepositoryAggregateId(UUID.randomUUID()), lookupKey);

    repository.save(aggregate);

    assertThat(repository.findByLookupKey(lookupKey)).containsSame(aggregate);
  }

  @Test
  void shouldReportWhetherAggregateExistsByDomainLookupValue() {
    TestRepositoryLookupKey existingLookupKey = new TestRepositoryLookupKey("existing@example.com");

    TestRepositoryLookupKey missingLookupKey = new TestRepositoryLookupKey("missing@example.com");

    repository.save(
        new TestRepositoryAggregate(
            new TestRepositoryAggregateId(UUID.randomUUID()), existingLookupKey));

    assertThat(repository.existsByLookupKey(existingLookupKey)).isTrue();
    assertThat(repository.existsByLookupKey(missingLookupKey)).isFalse();
  }

  @Test
  void shouldReturnEmptyWhenAggregateDoesNotExist() {
    TestRepositoryAggregateId missingId = new TestRepositoryAggregateId(UUID.randomUUID());

    TestRepositoryLookupKey missingLookupKey = new TestRepositoryLookupKey("missing@example.com");

    assertThat(repository.findById(missingId)).isEmpty();
    assertThat(repository.findByLookupKey(missingLookupKey)).isEmpty();
  }

  @Test
  void shouldReplaceStoredAggregateWhenSavingSameTypedIdentity() {
    TestRepositoryAggregateId aggregateId = new TestRepositoryAggregateId(UUID.randomUUID());

    TestRepositoryAggregate originalAggregate =
        new TestRepositoryAggregate(
            aggregateId, new TestRepositoryLookupKey("original@example.com"));

    TestRepositoryAggregate replacementAggregate =
        new TestRepositoryAggregate(
            aggregateId, new TestRepositoryLookupKey("replacement@example.com"));

    repository.save(originalAggregate);
    repository.save(replacementAggregate);

    assertThat(repository.findById(aggregateId)).containsSame(replacementAggregate);

    assertThat(repository.existsByLookupKey(new TestRepositoryLookupKey("original@example.com")))
        .isFalse();

    assertThat(repository.existsByLookupKey(new TestRepositoryLookupKey("replacement@example.com")))
        .isTrue();
  }

  @Test
  void shouldUseNormalizedDomainLookupValue() {
    TestRepositoryAggregate aggregate =
        new TestRepositoryAggregate(
            new TestRepositoryAggregateId(UUID.randomUUID()),
            new TestRepositoryLookupKey("  FIXTURE@EXAMPLE.COM  "));

    repository.save(aggregate);

    assertThat(repository.findByLookupKey(new TestRepositoryLookupKey("fixture@example.com")))
        .containsSame(aggregate);
  }

  @Test
  void shouldRejectNullAggregate() {
    assertThatNullPointerException()
        .isThrownBy(() -> repository.save(null))
        .withMessage("aggregate must not be null");
  }

  @Test
  void shouldRejectNullTypedIdentity() {
    assertThatNullPointerException()
        .isThrownBy(() -> repository.findById(null))
        .withMessage("aggregateId must not be null");
  }

  @Test
  void shouldRejectNullLookupValue() {
    assertThatNullPointerException()
        .isThrownBy(() -> repository.findByLookupKey(null))
        .withMessage("lookupKey must not be null");

    assertThatNullPointerException()
        .isThrownBy(() -> repository.existsByLookupKey(null))
        .withMessage("lookupKey must not be null");
  }
}
