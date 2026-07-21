package com.shoestore.shared.domain.model.fixture;

import com.shoestore.shared.persistence.BaseEntity;

import java.util.Objects;

/**
 * Child entity owned exclusively by {@link TestAggregateRoot}.
 *
 * <p>
 * This fixture verifies that child entity state can only be modified
 * through the aggregate root.
 * </p>
 */
public final class TestChildEntity extends BaseEntity {

    private String name;

    /**
     * Constructor reserved for persistence frameworks.
     */
    protected TestChildEntity() {
    }

    private TestChildEntity(String name) {
        this.name = requireName(name);
    }

    static TestChildEntity create(String name) {
        return new TestChildEntity(name);
    }

    void rename(String newName) {
        this.name = requireName(newName);
    }

    public String getName() {
        return name;
    }

    private static String requireName(String value) {
        Objects.requireNonNull(value, "child name must not be null");

        String normalizedValue = value.trim();

        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException(
                    "child name must not be blank"
            );
        }

        return normalizedValue;
    }
}
