package com.shoestore.modules.architecturefixture.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.shoestore.modules.architecturefixture.domain.model.TestRepositoryAggregate;
import com.shoestore.modules.architecturefixture.domain.model.TestRepositoryChild;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryAggregateId;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryChildId;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryLookupKey;
import com.shoestore.shared.domain.event.DomainEvent;
import com.shoestore.shared.domain.model.AggregateRoot;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AggregateRootOwnershipTest {

  @Test
  void aggregateShouldOwnItsChildEntities() {
    TestRepositoryChildId childId =
        new TestRepositoryChildId(UUID.randomUUID());

    TestRepositoryAggregate aggregate =
        createAggregate();

    aggregate.addChild(
        childId,
        "Primary child");

    assertThat(aggregate.children())
        .singleElement()
        .satisfies(
            child -> {
              assertThat(child.id()).isEqualTo(childId);
              assertThat(child.description())
                  .isEqualTo("Primary child");
            });
  }

  @Test
void childEntityShouldNotBeAnAggregateRoot() {
  assertThat(
          AggregateRoot.class.isAssignableFrom(
              TestRepositoryChild.class))
      .isFalse();
}

  @Test
  void childMutationShouldBeControlledByAggregateRoot()
      throws NoSuchMethodException {

    Method updateDescription =
        TestRepositoryChild.class.getDeclaredMethod(
            "updateDescription",
            String.class);

    assertThat(Modifier.isPublic(updateDescription.getModifiers()))
        .isFalse();

    assertThat(Modifier.isProtected(updateDescription.getModifiers()))
        .isFalse();

    assertThat(Modifier.isPrivate(updateDescription.getModifiers()))
        .isFalse();
  }

  @Test
  void aggregateShouldUpdateOwnedChildThroughAggregateBehavior() {
    TestRepositoryChildId childId =
        new TestRepositoryChildId(UUID.randomUUID());

    TestRepositoryAggregate aggregate =
        createAggregate();

    aggregate.addChild(
        childId,
        "Original description");

    aggregate.updateChildDescription(
        childId,
        "Updated description");

    assertThat(aggregate.findChild(childId))
        .get()
        .extracting(TestRepositoryChild::description)
        .isEqualTo("Updated description");
  }

  @Test
  void aggregateShouldRejectDuplicateChildIdentity() {
    TestRepositoryChildId childId =
        new TestRepositoryChildId(UUID.randomUUID());

    TestRepositoryAggregate aggregate =
        createAggregate();

    aggregate.addChild(
        childId,
        "First child");

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                aggregate.addChild(
                    childId,
                    "Duplicate child"))
        .withMessage(
            "child with the same identity already exists");
  }

  @Test
  void aggregateShouldRejectMutationOfUnownedChild() {
    TestRepositoryAggregate aggregate =
        createAggregate();

    TestRepositoryChildId unknownChildId =
        new TestRepositoryChildId(UUID.randomUUID());

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                aggregate.updateChildDescription(
                    unknownChildId,
                    "Updated description"))
        .withMessage(
            "child does not belong to aggregate");
  }

  @Test
  void aggregateShouldReturnImmutableChildCollection() {
    TestRepositoryAggregate aggregate =
        createAggregate();

    aggregate.addChild(
        new TestRepositoryChildId(UUID.randomUUID()),
        "Primary child");

    assertThat(aggregate.children())
        .isUnmodifiable();
  }

  @Test
  void repositoryShouldOperateOnlyOnItsAggregateRoot() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .allSatisfy(
            method -> {
              assertThat(
                      Arrays.asList(method.getParameterTypes()))
                  .doesNotContain(
                      TestRepositoryChild.class,
                      TestRepositoryChildId.class,
                      DomainEvent.class);
            });
  }

  @Test
  void repositoryShouldNeverReturnOwnedChildDirectly() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .allSatisfy(
            method ->
                assertThat(method.getGenericReturnType().getTypeName())
                    .doesNotContain(
                        TestRepositoryChild.class.getName(),
                        TestRepositoryChildId.class.getName()));
  }

  @Test
  void noRepositoryShouldBeDeclaredForChildEntity() {
    assertThat(
            repositoryClassesDeclaredInFixture())
        .containsExactly(TestAggregateRepository.class);
  }

  private TestRepositoryAggregate createAggregate() {
    return new TestRepositoryAggregate(
        new TestRepositoryAggregateId(UUID.randomUUID()),
        new TestRepositoryLookupKey(
            "ownership@example.com"));
  }

  private Class<?>[] repositoryClassesDeclaredInFixture() {
    return new Class<?>[] {
      TestAggregateRepository.class
    };
  }
}
