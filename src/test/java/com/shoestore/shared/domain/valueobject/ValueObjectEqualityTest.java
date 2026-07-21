package com.shoestore.shared.domain.valueobject;

import com.shoestore.shared.domain.valueobject.fixture.TestQuantity;
import com.shoestore.shared.domain.valueobject.fixture.TestScalarValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.HashSet;

class ValueObjectEqualityTest {

    @Test
    void shouldBeEqualWhenValuesAreEqual() {

        var first = new TestScalarValue("Nike");
        var second = new TestScalarValue("Nike");

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void shouldHaveSameHashCodeWhenValuesAreEqual() {

        var first = new TestScalarValue("Nike");
        var second = new TestScalarValue("Nike");

        assertThat(first.hashCode())
                .isEqualTo(second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenValuesDiffer() {

        var first = new TestScalarValue("Nike");
        var second = new TestScalarValue("Adidas");

        assertThat(first)
                .isNotEqualTo(second);
    }

    @Test
    void shouldBeEqualToItself() {

        var value = new TestScalarValue("Nike");

        assertThat(value)
                .isEqualTo(value);
    }

    @Test
    void shouldBeSymmetric() {

        var left = new TestScalarValue("Nike");

        var right = new TestScalarValue("Nike");

        assertThat(left.equals(right))
                .isTrue();

        assertThat(right.equals(left))
                .isTrue();
    }

    @Test
    void shouldBeTransitive() {

        var first = new TestScalarValue("Nike");
        var second = new TestScalarValue("Nike");
        var third = new TestScalarValue("Nike");

        assertThat(first)
                .isEqualTo(second);

        assertThat(second)
                .isEqualTo(third);

        assertThat(first)
                .isEqualTo(third);
    }

    @Test
    void shouldNotBeEqualToNull() {

        var value = new TestScalarValue("Nike");

        assertThat(value.equals(null))
                .isFalse();
    }

    @Test
    void shouldNotBeEqualToDifferentType() {

        var value = new TestScalarValue("Nike");

        assertThat(value.equals("Nike"))
                .isFalse();
    }

    @Test
    void shouldBehaveCorrectlyInsideHashSet() {

        var set = new HashSet<TestScalarValue>();

        set.add(new TestScalarValue("Nike"));
        set.add(new TestScalarValue("Nike"));

        assertThat(set)
                .hasSize(1);
    }

    @Test
    void shouldReplaceExistingValueInHashMap() {

        var map = new HashMap<TestScalarValue, Integer>();

        map.put(
                new TestScalarValue("Nike"),
                1);

        map.put(
                new TestScalarValue("Nike"),
                2);

        assertThat(map)
                .hasSize(1);

        assertThat(
                map.get(new TestScalarValue("Nike")))
                .isEqualTo(2);
    }

    @Test
    void shouldUseNormalizedValueForEquality() {

        var left = new TestScalarValue(" Nike ");

        var right = new TestScalarValue("Nike");

        assertThat(left)
                .isEqualTo(right);
    }

    @Test
    void quantityShouldUseValueEquality() {

        var first =
                new TestQuantity(5);

        var second =
                new TestQuantity(5);

        assertThat(first)
                .isEqualTo(second);
    }
}
