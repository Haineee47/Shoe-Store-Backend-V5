package com.shoestore.shared.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.shoestore.shared.domain.policy.fixture.StandardTestOrderCancellationPolicy;
import com.shoestore.shared.domain.policy.fixture.TestCancellationContext;
import com.shoestore.shared.domain.policy.fixture.TestCancellationDecision;
import com.shoestore.shared.domain.policy.fixture.TestCancellationOrderStatus;
import com.shoestore.shared.domain.policy.fixture.TestFulfillmentStatus;
import com.shoestore.shared.domain.policy.fixture.TestOrderCancellationPolicy;
import com.shoestore.shared.domain.policy.fixture.TestPaymentStatus;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DomainPolicyInputOutputConventionTest {

  private final TestOrderCancellationPolicy policy = new StandardTestOrderCancellationPolicy();

  @Test
  void cancellationContextShouldRemainARecord() {
    assertThat(TestCancellationContext.class.isRecord()).isTrue();
  }

  @Test
  void cancellationContextShouldExposeOnlyExpectedDomainComponents() {
    RecordComponent[] components = TestCancellationContext.class.getRecordComponents();

    assertThat(components)
        .extracting(RecordComponent::getName)
        .containsExactly("orderStatus", "paymentStatus", "fulfillmentStatus");

    assertThat(components).hasSize(3);

    assertThat(components[0].getType()).isEqualTo(TestCancellationOrderStatus.class);

    assertThat(components[1].getType()).isEqualTo(TestPaymentStatus.class);

    assertThat(components[2].getType()).isEqualTo(TestFulfillmentStatus.class);
  }

  @Test
  void cancellationContextShouldRejectNullOrderStatus() {
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                new TestCancellationContext(
                    null, TestPaymentStatus.PENDING, TestFulfillmentStatus.NOT_STARTED))
        .withMessage("orderStatus must not be null");
  }

  @Test
  void cancellationContextShouldRejectNullPaymentStatus() {
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                new TestCancellationContext(
                    TestCancellationOrderStatus.DRAFT, null, TestFulfillmentStatus.NOT_STARTED))
        .withMessage("paymentStatus must not be null");
  }

  @Test
  void cancellationContextShouldRejectNullFulfillmentStatus() {
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                new TestCancellationContext(
                    TestCancellationOrderStatus.DRAFT, TestPaymentStatus.PENDING, null))
        .withMessage("fulfillmentStatus must not be null");
  }

  @Test
  void policyShouldRejectNullContext() {
    assertThatNullPointerException()
        .isThrownBy(() -> policy.evaluate(null))
        .withMessage("context must not be null");
  }

  @Test
  void policyContractShouldAcceptTypedDomainContext() throws Exception {

    Method evaluateMethod =
        TestOrderCancellationPolicy.class.getDeclaredMethod(
            "evaluate", TestCancellationContext.class);

    assertThat(evaluateMethod.getParameterTypes()).containsExactly(TestCancellationContext.class);
  }

  @Test
  void policyContractShouldReturnTypedDomainDecision() throws Exception {

    Method evaluateMethod =
        TestOrderCancellationPolicy.class.getDeclaredMethod(
            "evaluate", TestCancellationContext.class);

    assertThat(evaluateMethod.getReturnType()).isEqualTo(TestCancellationDecision.class);
  }

  @Test
  void policyContractShouldDeclareOneBusinessDecisionMethod() {
    Method[] declaredMethods = TestOrderCancellationPolicy.class.getDeclaredMethods();

    assertThat(declaredMethods).hasSize(1);

    assertThat(declaredMethods[0].getName()).isEqualTo("evaluate");
  }

  @Test
  void policyShouldNeverReturnNullForValidDomainInput() {
    Arrays.stream(TestCancellationOrderStatus.values())
        .forEach(
            orderStatus ->
                Arrays.stream(TestPaymentStatus.values())
                    .forEach(
                        paymentStatus ->
                            Arrays.stream(TestFulfillmentStatus.values())
                                .forEach(
                                    fulfillmentStatus -> {
                                      TestCancellationContext context =
                                          new TestCancellationContext(
                                              orderStatus, paymentStatus, fulfillmentStatus);

                                      assertThat(policy.evaluate(context)).isNotNull();
                                    })));
  }

  @Test
  void policyShouldReturnOnlyDeclaredDecisionValues() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.SETTLED,
            TestFulfillmentStatus.PROCESSING);

    TestCancellationDecision decision = policy.evaluate(context);

    assertThat(Arrays.asList(TestCancellationDecision.values())).contains(decision);
  }

  @Test
  void evaluationShouldNotChangeContextValues() {
    TestCancellationContext context =
        new TestCancellationContext(
            TestCancellationOrderStatus.CONFIRMED,
            TestPaymentStatus.AUTHORIZED,
            TestFulfillmentStatus.PROCESSING);

    policy.evaluate(context);

    assertThat(context)
        .isEqualTo(
            new TestCancellationContext(
                TestCancellationOrderStatus.CONFIRMED,
                TestPaymentStatus.AUTHORIZED,
                TestFulfillmentStatus.PROCESSING));
  }

  @Test
  void decisionShouldRemainAnEnum() {
    assertThat(TestCancellationDecision.class.isEnum()).isTrue();
  }

  @Test
  void decisionShouldExposeOnlyBusinessDecisionConstants() {
    assertThat(TestCancellationDecision.values())
        .containsExactly(
            TestCancellationDecision.ALLOWED,
            TestCancellationDecision.REJECTED_ALREADY_CANCELLED,
            TestCancellationDecision.REJECTED_ALREADY_SHIPPED,
            TestCancellationDecision.REJECTED_PAYMENT_SETTLED);
  }
}
