package com.shoestore.shared.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shoestore.shared.domain.valueobject.fixture.TestTags;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValueObjectDefensiveCopyTest {

  @Test
  void shouldRejectNullCollection() {
    assertThatNullPointerException()
        .isThrownBy(() -> new TestTags(null))
        .withMessage("values must not be null");
  }

  @Test
  void shouldRejectNullElements() {
    var values = new ArrayList<String>();
    values.add("running");
    values.add(null);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new TestTags(values))
        .withMessage("values must not contain null elements");
  }

  @Test
  void shouldAcceptEmptyCollection() {
    var tags = new TestTags(List.of());

    assertThat(tags.values()).isEmpty();
  }

  @Test
  void shouldPreserveElementOrder() {
    var tags = new TestTags(List.of("running", "sport", "sale"));

    assertThat(tags.values()).containsExactly("running", "sport", "sale");
  }

  @Test
  void shouldCreateDefensiveCopyOfInputCollection() {
    var source = new ArrayList<String>();
    source.add("running");
    source.add("sport");

    var tags = new TestTags(source);

    source.add("sale");
    source.remove("running");

    assertThat(tags.values()).containsExactly("running", "sport");
  }

  @Test
  void shouldNotExposeMutableCollection() {
    var tags = new TestTags(List.of("running", "sport"));

    assertThatThrownBy(() -> tags.values().add("sale"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotAllowElementRemovalThroughAccessor() {
    var tags = new TestTags(List.of("running", "sport"));

    assertThatThrownBy(() -> tags.values().remove("running"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldNotAllowCollectionClearThroughAccessor() {
    var tags = new TestTags(List.of("running", "sport"));

    assertThatThrownBy(() -> tags.values().clear())
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldUseCollectionContentsForEquality() {
    var first = new TestTags(List.of("running", "sport"));

    var second = new TestTags(List.of("running", "sport"));

    assertThat(first).isEqualTo(second);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void shouldNotBeEqualWhenElementOrderDiffers() {
    var first = new TestTags(List.of("running", "sport"));

    var second = new TestTags(List.of("sport", "running"));

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void sourceMutationShouldNotChangeHashCode() {
    var source = new ArrayList<>(List.of("running", "sport"));

    var tags = new TestTags(source);
    var originalHashCode = tags.hashCode();

    source.add("sale");

    assertThat(tags.hashCode()).isEqualTo(originalHashCode);
  }
}
