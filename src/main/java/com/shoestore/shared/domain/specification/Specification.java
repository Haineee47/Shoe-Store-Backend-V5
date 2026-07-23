package com.shoestore.shared.domain.specification;

import java.util.Objects;

/**
 * Represents a reusable business predicate evaluated against a typed domain candidate.
 *
 * <p>A specification answers whether the supplied candidate satisfies a particular business
 * condition.
 *
 * @param <T> the candidate type evaluated by this specification
 */
@FunctionalInterface
public interface Specification<T> {

  /**
   * Determines whether the supplied candidate satisfies this business specification.
   *
   * @param candidate the candidate to evaluate
   * @return {@code true} when the candidate satisfies the specification; otherwise {@code false}
   */
  boolean isSatisfiedBy(T candidate);

  /**
   * Creates a specification that is satisfied only when both this specification and the supplied
   * specification are satisfied.
   *
   * <p>Evaluation preserves Java short-circuit semantics. The supplied specification is not
   * evaluated when this specification returns {@code false}.
   *
   * @param other the specification combined with this specification
   * @return the composed specification
   * @throws NullPointerException when {@code other} is {@code null}
   */
  default Specification<T> and(Specification<? super T> other) {

    Objects.requireNonNull(other, "other specification must not be null");

    return candidate -> isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
  }

  /**
   * Creates a specification that is satisfied when either this specification or the supplied
   * specification is satisfied.
   *
   * <p>Evaluation preserves Java short-circuit semantics. The supplied specification is not
   * evaluated when this specification returns {@code true}.
   *
   * @param other the specification combined with this specification
   * @return the composed specification
   * @throws NullPointerException when {@code other} is {@code null}
   */
  default Specification<T> or(Specification<? super T> other) {

    Objects.requireNonNull(other, "other specification must not be null");

    return candidate -> isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
  }

  /**
   * Creates a specification whose result is the logical negation of this specification.
   *
   * @return the negated specification
   */
  default Specification<T> not() {
    return candidate -> !isSatisfiedBy(candidate);
  }
}
