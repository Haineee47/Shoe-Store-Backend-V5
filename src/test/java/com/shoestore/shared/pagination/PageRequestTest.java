package com.shoestore.shared.pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageRequestTest {

  @Test
  void shouldCreatePageableWithoutSorting() {
    PageRequest request = PageRequest.of(1, 25);

    Pageable pageable = request.toPageable();

    assertThat(pageable.getPageNumber()).isEqualTo(1);
    assertThat(pageable.getPageSize()).isEqualTo(25);
    assertThat(pageable.getSort().isUnsorted()).isTrue();
  }

  @Test
  void shouldCreatePageableWithSorting() {
    PageRequest request =
        new PageRequest(0, 20, List.of(new PageSort("createdAt", PageSort.Direction.DESC)));

    Pageable pageable = request.toPageable();

    Sort.Order order = pageable.getSort().getOrderFor("createdAt");

    assertThat(order).isNotNull();
    assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
  }

  @Test
  void shouldRejectNegativePageIndex() {
    assertThatThrownBy(() -> PageRequest.of(-1, 20))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Page index must not be negative");
  }

  @Test
  void shouldRejectPageSizeGreaterThanMaximum() {
    assertThatThrownBy(() -> PageRequest.of(0, PageRequest.MAX_SIZE + 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Page size must not exceed " + PageRequest.MAX_SIZE);
  }
}
