package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.modules.architecturefixture.application.command.CreateTestProductCommand;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationCommandFixtureTest {

  @Test
  void shouldCreateImmutableCommandFromValidInput() {

    CreateTestProductCommand command =
        new CreateTestProductCommand(
            "Test Product", new BigDecimal("100.00"), List.of("shoe", "active"));

    assertThat(command.name()).isEqualTo("Test Product");
    assertThat(command.price()).isEqualByComparingTo("100.00");
    assertThat(command.tags()).containsExactly("shoe", "active");
  }

  @Test
  void shouldDefensivelyCopyCollectionInput() {

    List<String> sourceTags = new ArrayList<>(List.of("shoe", "active"));

    CreateTestProductCommand command =
        new CreateTestProductCommand("Test Product", new BigDecimal("100.00"), sourceTags);

    sourceTags.add("modified");

    assertThat(command.tags()).containsExactly("shoe", "active");
  }

  @Test
  void shouldExposeUnmodifiableCollection() {

    CreateTestProductCommand command =
        new CreateTestProductCommand("Test Product", new BigDecimal("100.00"), List.of("shoe"));

    assertThatThrownBy(() -> command.tags().add("modified"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldRejectNullName() {

    assertThatThrownBy(
            () -> new CreateTestProductCommand(null, new BigDecimal("100.00"), List.of()))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name must not be null");
  }

  @Test
  void shouldRejectNullPrice() {

    assertThatThrownBy(() -> new CreateTestProductCommand("Test Product", null, List.of()))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("price must not be null");
  }

  @Test
  void shouldRejectNullTags() {

    assertThatThrownBy(
            () -> new CreateTestProductCommand("Test Product", new BigDecimal("100.00"), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("tags must not be null");
  }
}
