package com.shoestore.shared.domain.model;

import com.shoestore.shared.domain.model.fixture.TestAggregateRoot;
import com.shoestore.shared.domain.model.fixture.TestAggregateStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AggregateFixtureConventionTest {

    @Test
    void shouldNormalizeAggregateNameDuringCreation() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("  Aggregate  ");

        assertThat(aggregate.getName())
                .isEqualTo("Aggregate");
    }

    @Test
    void shouldRejectNullAggregateName() {
        assertThatNullPointerException()
                .isThrownBy(() -> TestAggregateRoot.create(null))
                .withMessage("aggregate name must not be null");
    }

    @Test
    void shouldRejectBlankAggregateName() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> TestAggregateRoot.create("   "))
                .withMessage("aggregate name must not be blank");
    }

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
    void shouldRenameChildThroughAggregateRoot() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("Original");
        aggregate.renameChild(0, "Renamed");

        assertThat(aggregate.getChildren().getFirst().getName())
                .isEqualTo("Renamed");
    }

    @Test
    void shouldRejectChildMutationWhenAggregateIsInactive() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.deactivate();

        assertThatIllegalStateException()
                .isThrownBy(() -> aggregate.addChild("Child"))
                .withMessage(
                        "only active aggregate may modify children"
                );
    }

    @Test
    void shouldExposeUnmodifiableChildCollection() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.addChild("Child");

        assertThatThrownBy(
                () -> aggregate.getChildren().clear()
        ).isInstanceOf(UnsupportedOperationException.class);

        assertThat(aggregate.getChildren())
                .hasSize(1);
    }

    @Test
    void shouldPerformIdempotentDeactivation() {
        TestAggregateRoot aggregate =
                TestAggregateRoot.create("Aggregate");

        aggregate.deactivate();
        aggregate.deactivate();

        assertThat(aggregate.getStatus())
                .isEqualTo(TestAggregateStatus.INACTIVE);
    }
}
