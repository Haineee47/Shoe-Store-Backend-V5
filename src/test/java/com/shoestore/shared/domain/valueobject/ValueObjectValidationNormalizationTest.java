package com.shoestore.shared.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.shoestore.shared.domain.valueobject.fixture.TestQuantity;
import com.shoestore.shared.domain.valueobject.fixture.TestScalarValue;
import com.shoestore.shared.domain.valueobject.fixture.TestTags;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValueObjectValidationNormalizationTest {

  @Test
  void scalarValueShouldRejectNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> new TestScalarValue(null))
        .withMessage("value must not be null");
  }

  @Test
  void scalarValueShouldRejectEmptyValue() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new TestScalarValue(""))
        .withMessage("value must not be blank");
  }

  @Test
  void scalarValueShouldRejectWhitespaceOnlyValue() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new TestScalarValue("   "))
        .withMessage("value must not be blank");
  }

  @Test
  void scalarValueShouldTrimLeadingAndTrailingWhitespace() {
    var value = new TestScalarValue("  Nike  ");

    assertThat(value.value()).isEqualTo("Nike");
  }

  @Test
  void scalarValueShouldPreserveValidContent() {
    var value = new TestScalarValue("Nike Air Max");

    assertThat(value.value()).isEqualTo("Nike Air Max");
  }

  @Test
  void scalarValueNormalizationShouldBeIdempotent() {
    var first = new TestScalarValue("  Nike  ");
    var second = new TestScalarValue(first.value());

    assertThat(second).isEqualTo(first);

    assertThat(second.value()).isEqualTo("Nike");
  }

  @Test
  void scalarValueShouldUseNormalizedValueForEquality() {
    var normalized = new TestScalarValue("Nike");
    var unnormalized = new TestScalarValue("  Nike  ");

    assertThat(unnormalized).isEqualTo(normalized);

    assertThat(unnormalized.hashCode()).isEqualTo(normalized.hashCode());
  }

  @Test
  void scalarValueShouldNotChangeInternalWhitespace() {
    var value = new TestScalarValue("Nike   Air");

    assertThat(value.value()).isEqualTo("Nike   Air");
  }

  @Test
  void quantityShouldRejectZero() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new TestQuantity(0))
        .withMessage("quantity must be positive");
  }

  @Test
  void quantityShouldRejectNegativeValue() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new TestQuantity(-1))
        .withMessage("quantity must be positive");
  }

  @Test
  void quantityShouldAcceptMinimumValidValue() {
    var quantity = new TestQuantity(1);

    assertThat(quantity.value()).isEqualTo(1);
  }

  @Test
  void quantityShouldAcceptPositiveValue() {
    var quantity = new TestQuantity(10);

    assertThat(quantity.value()).isEqualTo(10);
  }

  @Test
  void tagsShouldRejectNullCollection() {
    assertThatNullPointerException().isThrownBy(() -> new TestTags(null));
  }

  @Test
  void tagsShouldAcceptEmptyCollection() {
    var tags = new TestTags(List.of());

    assertThat(tags.values()).isEmpty();
  }

  @Test
  void tagsShouldPreserveValidValues() {
    var tags = new TestTags(List.of("sport", "running"));

    assertThat(tags.values()).containsExactly("sport", "running");
  }
}
