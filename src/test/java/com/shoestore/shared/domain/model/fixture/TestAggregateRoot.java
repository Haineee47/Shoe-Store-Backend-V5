package com.shoestore.shared.domain.model.fixture;

import com.shoestore.shared.domain.model.AggregateRoot;
import com.shoestore.shared.persistence.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate test fixture used to verify aggregate-root and domain-entity
 * conventions.
 */
public final class TestAggregateRoot
        extends BaseEntity
        implements AggregateRoot {

    private String name;

    private TestAggregateStatus status;

    private final List<TestChildEntity> children = new ArrayList<>();

    /**
     * Constructor reserved for persistence frameworks.
     */
    protected TestAggregateRoot() {
    }

    private TestAggregateRoot(String name) {
        this.name = requireName(name);
        this.status = TestAggregateStatus.ACTIVE;
    }

    public static TestAggregateRoot create(String name) {
        return new TestAggregateRoot(name);
    }

    public void rename(String newName) {
        ensureNotArchived();

        this.name = requireName(newName);
    }

    public void activate() {
        if (status == TestAggregateStatus.ACTIVE) {
            return;
        }

        ensureNotArchived();

        status = TestAggregateStatus.ACTIVE;
    }

    public void deactivate() {
        if (status == TestAggregateStatus.INACTIVE) {
            return;
        }

        ensureNotArchived();

        status = TestAggregateStatus.INACTIVE;
    }

    public void archive() {
        if (status == TestAggregateStatus.ARCHIVED) {
            return;
        }

        status = TestAggregateStatus.ARCHIVED;
    }

    public void addChild(String childName) {
        ensureActive();

        children.add(TestChildEntity.create(childName));
    }

    public void renameChild(int position, String newName) {
        ensureActive();

        childAt(position).rename(newName);
    }

    public void removeChild(int position) {
        ensureActive();

        children.remove(requireValidPosition(position));
    }

    public int childCount() {
        return children.size();
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public String getName() {
        return name;
    }

    public TestAggregateStatus getStatus() {
        return status;
    }

    public List<TestChildEntity> getChildren() {
        return List.copyOf(children);
    }

    private TestChildEntity childAt(int position) {
        return children.get(requireValidPosition(position));
    }

    private int requireValidPosition(int position) {
        if (position < 0 || position >= children.size()) {
            throw new IllegalArgumentException(
                    "child position is outside aggregate boundaries"
            );
        }

        return position;
    }

    private void ensureActive() {
        if (status != TestAggregateStatus.ACTIVE) {
            throw new IllegalStateException(
                    "only active aggregate may modify children"
            );
        }
    }

    private void ensureNotArchived() {
        if (status == TestAggregateStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "archived aggregate cannot change lifecycle state"
            );
        }
    }

    private static String requireName(String value) {
        Objects.requireNonNull(
                value,
                "aggregate name must not be null"
        );

        String normalizedValue = value.trim();

        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException(
                    "aggregate name must not be blank"
            );
        }

        return normalizedValue;
    }
}
