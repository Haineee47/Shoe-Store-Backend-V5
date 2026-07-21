package com.shoestore.shared.domain.model;

import com.shoestore.shared.domain.model.fixture.TestAggregateRoot;
import com.shoestore.shared.domain.model.fixture.TestChildEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AggregateCollectionEncapsulationTest {

    @Test
    void shouldCreateChildThroughAggregateRoot() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("Child one");

        assertThat(aggregate.getChildren())
                .hasSize(1);

        assertThat(aggregate.getChildren().getFirst().getName())
                .isEqualTo("Child one");
    }

    @Test
    void shouldNormalizeChildNameDuringCreation() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("  Child one  ");

        assertThat(aggregate.getChildren().getFirst().getName())
                .isEqualTo("Child one");
    }

    @Test
    void shouldRenameChildThroughAggregateRoot() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("Original");
        aggregate.renameChild(0, "Renamed");

        assertThat(aggregate.getChildren().getFirst().getName())
                .isEqualTo("Renamed");
    }

    @Test
    void shouldRemoveChildThroughAggregateRoot() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("Child");
        aggregate.removeChild(0);

        assertThat(aggregate.getChildren())
                .isEmpty();
    }

    @Test
    void shouldPreserveRemainingChildrenAfterRemoval() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("First");
        aggregate.addChild("Second");

        aggregate.removeChild(0);

        assertThat(aggregate.getChildren())
                .singleElement()
                .extracting(TestChildEntity::getName)
                .isEqualTo("Second");
    }

    @Test
    void shouldExposeUnmodifiableCollection() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("Child");

        List<TestChildEntity> exposedChildren =
                aggregate.getChildren();

        assertThatThrownBy(exposedChildren::clear)
                .isInstanceOf(UnsupportedOperationException.class);

        assertThat(aggregate.getChildren())
                .hasSize(1);
    }

    @Test
    void shouldReturnCollectionSnapshot() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("First");

        List<TestChildEntity> snapshot =
                aggregate.getChildren();

        aggregate.addChild("Second");

        assertThat(snapshot)
                .hasSize(1);

        assertThat(aggregate.getChildren())
                .hasSize(2);
    }

    @Test
    void shouldRejectNegativeChildPosition() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        assertThatIllegalArgumentException()
                .isThrownBy(() ->
                        aggregate.renameChild(-1, "Renamed")
                )
                .withMessage(
                        "child position is outside aggregate boundaries"
                );
    }

    @Test
    void shouldRejectPositionEqualToCollectionSize() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("Child");

        assertThatIllegalArgumentException()
                .isThrownBy(() ->
                        aggregate.renameChild(1, "Renamed")
                )
                .withMessage(
                        "child position is outside aggregate boundaries"
                );
    }

    @Test
    void shouldPreserveCollectionWhenPositionIsInvalid() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("Child");

        assertThatIllegalArgumentException()
                .isThrownBy(() ->
                        aggregate.removeChild(5)
                );

        assertThat(aggregate.getChildren())
                .singleElement()
                .extracting(TestChildEntity::getName)
                .isEqualTo("Child");
    }

    @Test
    void shouldRejectAddingChildWhenInactive() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.deactivate();

        assertThatIllegalStateException()
                .isThrownBy(() ->
                        aggregate.addChild("Child")
                )
                .withMessage(
                        "only active aggregate may modify children"
                );

        assertThat(aggregate.getChildren())
                .isEmpty();
    }

    @Test
    void shouldRejectRenamingChildWhenInactive() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("Child");
        aggregate.deactivate();

        assertThatIllegalStateException()
                .isThrownBy(() ->
                        aggregate.renameChild(0, "Renamed")
                )
                .withMessage(
                        "only active aggregate may modify children"
                );

        assertThat(aggregate.getChildren().getFirst().getName())
                .isEqualTo("Child");
    }

    @Test
    void shouldRejectRemovingChildWhenInactive() {
        TestAggregateRoot aggregate = TestAggregateRoot.create("Aggregate");

        aggregate.addChild("Child");
        aggregate.deactivate();

        assertThatIllegalStateException()
                .isThrownBy(() -> aggregate.removeChild(0))
                .withMessage(
                        "only active aggregate may modify children");

        assertThat(aggregate.getChildren())
                .hasSize(1);
    }

    @Test
    void shouldDeclareChildrenCollectionPrivateAndFinal()
            throws NoSuchFieldException {

        var field =
                TestAggregateRoot.class.getDeclaredField("children");

        assertThat(Modifier.isPrivate(field.getModifiers()))
                .isTrue();

        assertThat(Modifier.isFinal(field.getModifiers()))
                .isTrue();
    }
}
