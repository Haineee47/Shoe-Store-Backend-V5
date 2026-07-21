package com.shoestore.shared.domain.valueobject.fixture;

import java.util.List;
import java.util.Objects;

public record TestTags(List<String> values) {

    public TestTags {
        Objects.requireNonNull(
                values,
                "values must not be null"
        );

        if (values.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "values must not contain null elements"
            );
        }

        values = List.copyOf(values);
    }
}
