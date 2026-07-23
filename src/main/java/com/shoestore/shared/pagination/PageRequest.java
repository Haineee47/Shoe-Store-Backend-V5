package com.shoestore.shared.pagination;

import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Shared pagination request used by application use cases.
 *
 * <p>Page indexes are zero-based.
 *
 * @param page zero-based page index
 * @param size maximum number of elements per page
 * @param sorts sorting criteria
 */
public record PageRequest(int page, int size, List<PageSort> sorts) {

  public static final int DEFAULT_PAGE = 0;
  public static final int DEFAULT_SIZE = 20;
  public static final int MAX_SIZE = 100;

  public PageRequest {
    if (page < 0) {
      throw new IllegalArgumentException("Page index must not be negative");
    }

    if (size < 1) {
      throw new IllegalArgumentException("Page size must be greater than zero");
    }

    if (size > MAX_SIZE) {
      throw new IllegalArgumentException("Page size must not exceed " + MAX_SIZE);
    }

    sorts = sorts == null ? List.of() : sorts.stream().filter(Objects::nonNull).toList();
  }

  /** Creates the default pagination request. */
  public static PageRequest defaultPage() {
    return new PageRequest(DEFAULT_PAGE, DEFAULT_SIZE, List.of());
  }

  /** Creates a pagination request without sorting. */
  public static PageRequest of(int page, int size) {
    return new PageRequest(page, size, List.of());
  }

  /** Converts this shared request to a Spring Data Pageable. */
  public Pageable toPageable() {
    if (sorts.isEmpty()) {
      return org.springframework.data.domain.PageRequest.of(page, size);
    }

    Sort sort = Sort.by(sorts.stream().map(PageSort::toOrder).toList());

    return org.springframework.data.domain.PageRequest.of(page, size, sort);
  }
}
