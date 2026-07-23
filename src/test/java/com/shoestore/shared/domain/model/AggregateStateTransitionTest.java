package com.shoestore.shared.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.shoestore.shared.domain.model.fixture.TestAggregateRoot;
import com.shoestore.shared.domain.model.fixture.TestAggregateStatus;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AggregateStateTransitionTest {

  @Test
  void shouldCreateAggregateInActiveState() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.ACTIVE);
  }

  @Test
  void shouldDeactivateActiveAggregate() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.deactivate();

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.INACTIVE);
  }

  @Test
  void shouldActivateInactiveAggregate() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.deactivate();
    aggregate.activate();

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.ACTIVE);
  }

  @Test
  void shouldArchiveActiveAggregate() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.archive();

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.ARCHIVED);
  }

  @Test
  void shouldArchiveInactiveAggregate() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.deactivate();
    aggregate.archive();

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.ARCHIVED);
  }

  @Test
  void shouldKeepRepeatedActivationIdempotent() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.activate();
    aggregate.activate();

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.ACTIVE);
  }

  @Test
  void shouldKeepRepeatedDeactivationIdempotent() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.deactivate();
    aggregate.deactivate();

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.INACTIVE);
  }

  @Test
  void shouldKeepRepeatedArchivingIdempotent() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.archive();
    aggregate.archive();

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.ARCHIVED);
  }

  @Test
  void shouldRejectActivationOfArchivedAggregate() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.archive();

    assertThatIllegalStateException()
        .isThrownBy(aggregate::activate)
        .withMessage("archived aggregate cannot change lifecycle state");

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.ARCHIVED);
  }

  @Test
  void shouldRejectDeactivationOfArchivedAggregate() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.archive();

    assertThatIllegalStateException()
        .isThrownBy(aggregate::deactivate)
        .withMessage("archived aggregate cannot change lifecycle state");

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.ARCHIVED);
  }

  @Test
  void shouldRejectRenameOfArchivedAggregate() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.archive();

    assertThatIllegalStateException()
        .isThrownBy(() -> aggregate.rename("Renamed"))
        .withMessage("archived aggregate cannot change lifecycle state");

    assertThat(aggregate.getName()).isEqualTo("Aggregate");
  }

  @Test
  void shouldRejectChildMutationWhenAggregateIsInactive() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.deactivate();

    assertThatIllegalStateException()
        .isThrownBy(() -> aggregate.addChild("Child"))
        .withMessage("only active aggregate may modify children");
  }

  @Test
  void shouldRejectChildMutationWhenAggregateIsArchived() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.archive();

    assertThatIllegalStateException()
        .isThrownBy(() -> aggregate.addChild("Child"))
        .withMessage("only active aggregate may modify children");
  }

  @Test
  void shouldPreserveStateWhenTransitionIsRejected() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

    aggregate.archive();

    assertThatIllegalStateException().isThrownBy(aggregate::activate);

    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.ARCHIVED);
  }

  @Test
  void shouldNotExposeGenericStatusMutationMethod() {
    boolean hasGenericStatusMutation =
        Arrays.stream(TestAggregateRoot.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .anyMatch(
                name ->
                    name.equals("setStatus")
                        || name.equals("changeStatus")
                        || name.equals("updateStatus"));

    assertThat(hasGenericStatusMutation).isFalse();
  }
}
