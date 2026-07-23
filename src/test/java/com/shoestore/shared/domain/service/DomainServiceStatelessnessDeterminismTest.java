package com.shoestore.shared.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.domain.service.fixture.TestAllocationDecision;
import com.shoestore.shared.domain.service.fixture.TestAllocationPolicy;
import com.shoestore.shared.domain.service.fixture.TestAllocationQuantity;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DomainServiceStatelessnessDeterminismTest {

  @Test
  void shouldDeclareNoInstanceFields() {
    Field[] instanceFields =
        Arrays.stream(TestAllocationPolicy.class.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .toArray(Field[]::new);

    assertThat(instanceFields).as("Domain Service fixture must not hold instance state").isEmpty();
  }

  @Test
  void shouldProduceEquivalentDecisionForEquivalentInputs() {
    TestAllocationPolicy policy = new TestAllocationPolicy();

    TestAllocationDecision firstDecision =
        policy.allocate(new TestAllocationQuantity(12), new TestAllocationQuantity(7));

    TestAllocationDecision secondDecision =
        policy.allocate(new TestAllocationQuantity(12), new TestAllocationQuantity(7));

    assertThat(firstDecision).isEqualTo(secondDecision);

    assertThat(firstDecision.hashCode()).isEqualTo(secondDecision.hashCode());
  }

  @Test
  void shouldProduceEquivalentDecisionAcrossDifferentServiceInstances() {
    TestAllocationPolicy firstPolicy = new TestAllocationPolicy();

    TestAllocationPolicy secondPolicy = new TestAllocationPolicy();

    TestAllocationDecision firstDecision =
        firstPolicy.allocate(new TestAllocationQuantity(9), new TestAllocationQuantity(4));

    TestAllocationDecision secondDecision =
        secondPolicy.allocate(new TestAllocationQuantity(9), new TestAllocationQuantity(4));

    assertThat(firstDecision).isEqualTo(secondDecision);
  }

  @Test
  void shouldNotDependOnPreviousInvocation() {
    TestAllocationPolicy policy = new TestAllocationPolicy();

    TestAllocationDecision firstTargetDecision =
        policy.allocate(new TestAllocationQuantity(10), new TestAllocationQuantity(6));

    policy.allocate(new TestAllocationQuantity(3), new TestAllocationQuantity(20));

    policy.allocate(new TestAllocationQuantity(8), new TestAllocationQuantity(0));

    TestAllocationDecision secondTargetDecision =
        policy.allocate(new TestAllocationQuantity(10), new TestAllocationQuantity(6));

    assertThat(secondTargetDecision).isEqualTo(firstTargetDecision);
  }

  @Test
  void shouldNotDependOnInvocationOrder() {
    TestAllocationPolicy firstPolicy = new TestAllocationPolicy();

    TestAllocationDecision firstCaseFromFirstOrder =
        firstPolicy.allocate(new TestAllocationQuantity(5), new TestAllocationQuantity(10));

    TestAllocationDecision secondCaseFromFirstOrder =
        firstPolicy.allocate(new TestAllocationQuantity(10), new TestAllocationQuantity(3));

    TestAllocationPolicy secondPolicy = new TestAllocationPolicy();

    TestAllocationDecision secondCaseFromReverseOrder =
        secondPolicy.allocate(new TestAllocationQuantity(10), new TestAllocationQuantity(3));

    TestAllocationDecision firstCaseFromReverseOrder =
        secondPolicy.allocate(new TestAllocationQuantity(5), new TestAllocationQuantity(10));

    assertThat(firstCaseFromFirstOrder).isEqualTo(firstCaseFromReverseOrder);

    assertThat(secondCaseFromFirstOrder).isEqualTo(secondCaseFromReverseOrder);
  }

  @Test
  void shouldReturnIndependentDecisionInstances() {
    TestAllocationPolicy policy = new TestAllocationPolicy();

    TestAllocationDecision firstDecision =
        policy.allocate(new TestAllocationQuantity(7), new TestAllocationQuantity(5));

    TestAllocationDecision secondDecision =
        policy.allocate(new TestAllocationQuantity(7), new TestAllocationQuantity(5));

    assertThat(firstDecision).isEqualTo(secondDecision);

    assertThat(firstDecision).isNotSameAs(secondDecision);
  }

  @Test
  void shouldRemainDeterministicAcrossRepeatedInvocations() {
    TestAllocationPolicy policy = new TestAllocationPolicy();

    TestAllocationDecision expected =
        policy.allocate(new TestAllocationQuantity(25), new TestAllocationQuantity(11));

    for (int invocation = 0; invocation < 100; invocation++) {
      TestAllocationDecision actual =
          policy.allocate(new TestAllocationQuantity(25), new TestAllocationQuantity(11));

      assertThat(actual).isEqualTo(expected);
    }
  }
}
