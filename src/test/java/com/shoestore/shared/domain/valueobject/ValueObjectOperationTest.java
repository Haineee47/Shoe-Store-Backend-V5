package com.shoestore.shared.domain.valueobject;

import com.shoestore.shared.domain.valueobject.fixture.TestQuantity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ValueObjectOperationTest {

    @Test
    void shouldIncreaseQuantity() {
        var quantity = new TestQuantity(5);
        var amount = new TestQuantity(3);

        var result = quantity.increaseBy(amount);

        assertThat(result)
                .isEqualTo(new TestQuantity(8));
    }

    @Test
    void shouldNotMutateOriginalQuantity() {
        var original = new TestQuantity(5);
        var amount = new TestQuantity(3);

        original.increaseBy(amount);

        assertThat(original)
                .isEqualTo(new TestQuantity(5));
    }

    @Test
    void shouldReturnDifferentInstance() {
        var original = new TestQuantity(5);

        var result = original.increaseBy(
                new TestQuantity(3)
        );

        assertThat(result)
                .isNotSameAs(original);
    }

    @Test
    void shouldUseValueEqualityForOperationResult() {
        var result = new TestQuantity(5)
                .increaseBy(new TestQuantity(3));

        var expected = new TestQuantity(8);

        assertThat(result)
                .isEqualTo(expected);

        assertThat(result.hashCode())
                .isEqualTo(expected.hashCode());
    }

    @Test
    void shouldRejectNullAmount() {
        var quantity = new TestQuantity(5);

        assertThatNullPointerException()
                .isThrownBy(() -> quantity.increaseBy(null))
                .withMessage("amount must not be null");
    }

    @Test
    void shouldRejectArithmeticOverflow() {
        var quantity = new TestQuantity(Integer.MAX_VALUE);
        var amount = new TestQuantity(1);

        assertThatExceptionOfType(ArithmeticException.class)
                .isThrownBy(() -> quantity.increaseBy(amount));
    }

    @Test
    void failedOperationShouldPreserveOriginalValue() {
        var original = new TestQuantity(Integer.MAX_VALUE);

        assertThatExceptionOfType(ArithmeticException.class)
                .isThrownBy(() ->
                        original.increaseBy(new TestQuantity(1))
                );

        assertThat(original)
                .isEqualTo(new TestQuantity(Integer.MAX_VALUE));
    }

    @Test
    void repeatedOperationsShouldRemainImmutable() {
        var original = new TestQuantity(2);

        var first = original.increaseBy(
                new TestQuantity(3)
        );

        var second = first.increaseBy(
                new TestQuantity(4)
        );

        assertThat(original)
                .isEqualTo(new TestQuantity(2));

        assertThat(first)
                .isEqualTo(new TestQuantity(5));

        assertThat(second)
                .isEqualTo(new TestQuantity(9));
    }
}
