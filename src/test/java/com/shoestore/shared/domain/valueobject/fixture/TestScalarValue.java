package com.shoestore.shared.domain.valueobject.fixture;

import java.util.Objects;

public record TestScalarValue(String value) {

    public TestScalarValue {
        Objects.requireNonNull(
                value,
                "value must not be null"
        );

        value = value.trim();

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "value must not be blank"
            );
        }
    }
}
