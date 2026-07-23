package com.shoestore.shared.pagination;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * Standard paginated result returned by application use cases.
 *
 * @param content page content
 * @param page zero-based current page index
 * @param size requested page size
 * @param numberOfItems number of items in the current page
 * @param totalItems total number of matching items
 * @param totalPages total number of pages
 * @param first whether this is the first page
 * @param last whether this is the last page
 * @param hasNext whether a following page exists
 * @param hasPrevious whether a previous page exists
 * @param <T> content type
 */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    int numberOfItems,
    long totalItems,
    int totalPages,
    boolean first,
    boolean last,
    boolean hasNext,
    boolean hasPrevious) {

  public PageResponse {
    content = content == null ? List.of() : List.copyOf(content);

    if (page < 0) {
      throw new IllegalArgumentException("Page index must not be negative");
    }

    if (size < 0) {
      throw new IllegalArgumentException("Page size must not be negative");
    }

    if (numberOfItems < 0) {
      throw new IllegalArgumentException("Number of items must not be negative");
    }

    if (totalItems < 0) {
      throw new IllegalArgumentException("Total items must not be negative");
    }

    if (totalPages < 0) {
      throw new IllegalArgumentException("Total pages must not be negative");
    }
  }

  /** Converts a Spring Data Page directly to the shared response model. */
  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getNumberOfElements(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast(),
        page.hasNext(),
        page.hasPrevious());
  }

  /** Converts and maps the content of a Spring Data Page. */
  public static <S, T> PageResponse<T> from(Page<S> page, Function<? super S, ? extends T> mapper) {
    Page<T> mappedPage = page.map(mapper);
    return from(mappedPage);
  }
}
