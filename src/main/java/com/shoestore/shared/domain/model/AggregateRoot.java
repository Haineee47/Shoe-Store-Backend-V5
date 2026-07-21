package com.shoestore.shared.domain.model;

/**
 * Marks a domain entity as the root of an aggregate.
 *
 * <p>
 * An aggregate root is the only entry point through which external
 * components may modify the state of the aggregate.
 * </p>
 *
 * <p>
 * Implementations may extend either
 * {@code BaseEntity} or {@code AuditableEntity}, depending on whether
 * persistence auditing is required.
 * </p>
 */
public interface AggregateRoot {
}
