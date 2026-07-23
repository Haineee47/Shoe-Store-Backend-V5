package com.shoestore.shared.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.domain.model.fixture.TestAggregateRoot;
import com.shoestore.shared.domain.model.fixture.TestAggregateStatus;
import com.shoestore.shared.persistence.BaseEntity;
import org.junit.jupiter.api.Test;

class AggregateRootContractTest {

  @Test
  void shouldMarkDomainEntityAsAggregateRoot() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Test aggregate");

    assertThat(aggregate).isInstanceOf(AggregateRoot.class);
  }

  @Test
  void shouldReuseBaseEntityIdentityAndVersion() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Test aggregate");

    assertThat(aggregate).isInstanceOf(BaseEntity.class);

    assertThat(aggregate.getId()).isNull();
    assertThat(aggregate.getVersion()).isZero();
    assertThat(aggregate.isPersisted()).isFalse();
  }

  @Test
  void shouldCreateAggregateInValidInitialState() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Test aggregate");

    assertThat(aggregate.getName()).isEqualTo("Test aggregate");

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.ACTIVE);

    assertThat(aggregate.getChildren()).isEmpty();
  }
}
