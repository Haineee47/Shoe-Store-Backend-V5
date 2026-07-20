package com.shoestore.shared.pagination;

import org.springframework.data.domain.Sort;

import java.util.Locale;

/**
 * Represents a sorting criterion for a paginated query.
 *
 * @param property  entity property used for sorting
 * @param direction sorting direction
 */
public record PageSort(
        String property,
        Direction direction
) {

    private static final Direction DEFAULT_DIRECTION = Direction.ASC;

    public PageSort {
        if (property == null || property.isBlank()) {
            throw new IllegalArgumentException(
                    "Sort property must not be blank"
            );
        }

        property = property.trim();
        direction = direction == null
                ? DEFAULT_DIRECTION
                : direction;
    }

    /**
     * Converts this shared sorting model to Spring Data Sort.Order.
     */
    public Sort.Order toOrder() {
        return switch (direction) {
            case ASC -> Sort.Order.asc(property);
            case DESC -> Sort.Order.desc(property);
        };
    }

    /**
     * Supported sorting directions.
     */
    public enum Direction {
        ASC,
        DESC;

        public static Direction from(String value) {
            if (value == null || value.isBlank()) {
                return DEFAULT_DIRECTION;
            }

            try {
                return Direction.valueOf(
                        value.trim().toUpperCase(Locale.ROOT)
                );
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Unsupported sort direction: " + value,
                        exception
                );
            }
        }
    }
}
