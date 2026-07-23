package com.shoestore.shared.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PageResponseTest {

  @Test
  void shouldCreateResponseFromSpringDataPage() {
    org.springframework.data.domain.Page<String> source =
        new PageImpl<>(List.of("shoe-1", "shoe-2"), PageRequest.of(1, 2), 5);

    PageResponse<String> response = PageResponse.from(source);

    assertThat(response.content()).containsExactly("shoe-1", "shoe-2");

    assertThat(response.page()).isEqualTo(1);
    assertThat(response.size()).isEqualTo(2);
    assertThat(response.numberOfItems()).isEqualTo(2);
    assertThat(response.totalItems()).isEqualTo(5);
    assertThat(response.totalPages()).isEqualTo(3);
    assertThat(response.first()).isFalse();
    assertThat(response.last()).isFalse();
    assertThat(response.hasNext()).isTrue();
    assertThat(response.hasPrevious()).isTrue();
  }

  @Test
  void shouldMapPageContent() {
    org.springframework.data.domain.Page<Integer> source =
        new PageImpl<>(List.of(1, 2, 3), PageRequest.of(0, 3), 3);

    PageResponse<String> response = PageResponse.from(source, value -> "item-" + value);

    assertThat(response.content()).containsExactly("item-1", "item-2", "item-3");
  }
}
