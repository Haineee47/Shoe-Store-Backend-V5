package com.shoestore.shared.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.domain.model.AggregateRoot;
import com.shoestore.shared.domain.policy.fixture.StandardTestOrderCancellationPolicy;
import com.shoestore.shared.domain.policy.fixture.TestCancellationContext;
import com.shoestore.shared.domain.policy.fixture.TestCancellationDecision;
import com.shoestore.shared.domain.policy.fixture.TestOrderCancellationPolicy;
import com.shoestore.shared.persistence.BaseEntity;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DomainPolicyDependencyBoundaryTest {

  @Test
  void policyContractShouldRemainInsideDomainPolicyPackage() {
    assertThat(TestOrderCancellationPolicy.class.getPackageName())
        .isEqualTo("com.shoestore.shared.domain.policy.fixture");
  }

  @Test
  void policyImplementationShouldRemainInsideDomainPolicyPackage() {
    assertThat(StandardTestOrderCancellationPolicy.class.getPackageName())
        .isEqualTo("com.shoestore.shared.domain.policy.fixture");
  }

  @Test
  void policyImplementationShouldNotExtendPersistenceBaseEntity() {
    assertThat(BaseEntity.class.isAssignableFrom(StandardTestOrderCancellationPolicy.class))
        .isFalse();
  }

  @Test
  void policyImplementationShouldNotImplementAggregateRoot() {
    assertThat(AggregateRoot.class.isAssignableFrom(StandardTestOrderCancellationPolicy.class))
        .isFalse();
  }

  @Test
  void policyImplementationShouldImplementOnlyItsBusinessPolicyContract() {
    assertThat(StandardTestOrderCancellationPolicy.class.getInterfaces())
        .containsExactly(TestOrderCancellationPolicy.class);
  }

  @Test
  void policyImplementationShouldDeclareNoDependencyFields() {
    Field[] declaredFields = StandardTestOrderCancellationPolicy.class.getDeclaredFields();

    assertThat(declaredFields).isEmpty();
  }

  @Test
  void policyImplementationShouldRequireNoConstructorDependencies() {
    Constructor<?>[] constructors =
        StandardTestOrderCancellationPolicy.class.getDeclaredConstructors();

    assertThat(constructors).hasSize(1);

    assertThat(constructors[0].getParameterTypes()).isEmpty();
  }

  @Test
  void policyContractShouldHaveNoFrameworkAnnotations() {
    Annotation[] annotations = TestOrderCancellationPolicy.class.getDeclaredAnnotations();

    assertThat(annotations).isEmpty();
  }

  @Test
  void policyImplementationShouldHaveNoFrameworkAnnotations() {
    Annotation[] annotations = StandardTestOrderCancellationPolicy.class.getDeclaredAnnotations();

    assertThat(annotations).isEmpty();
  }

  @Test
  void policyEvaluationMethodShouldUseOnlyTypedDomainInputAndOutput() throws Exception {

    Method method =
        StandardTestOrderCancellationPolicy.class.getDeclaredMethod(
            "evaluate", TestCancellationContext.class);

    assertThat(method.getParameterTypes()).containsExactly(TestCancellationContext.class);

    assertThat(method.getReturnType()).isEqualTo(TestCancellationDecision.class);
  }

  @Test
  void policyEvaluationMethodShouldNotDeclareCheckedExceptions() throws Exception {

    Method method =
        StandardTestOrderCancellationPolicy.class.getDeclaredMethod(
            "evaluate", TestCancellationContext.class);

    assertThat(method.getExceptionTypes()).isEmpty();
  }

  @Test
  void policyImplementationShouldNotReferenceForbiddenPackagesThroughItsApi() {
    Class<?>[] referencedTypes =
        Arrays.stream(StandardTestOrderCancellationPolicy.class.getDeclaredMethods())
            .flatMap(
                method ->
                    Arrays.stream(
                        concatenate(
                            method.getParameterTypes(), new Class<?>[] {method.getReturnType()})))
            .distinct()
            .toArray(Class<?>[]::new);

    assertThat(referencedTypes)
        .allSatisfy(
            type ->
                assertThat(type.getPackageName())
                    .doesNotStartWith("org.springframework")
                    .doesNotStartWith("jakarta.persistence")
                    .doesNotStartWith("org.hibernate")
                    .doesNotStartWith("java.sql")
                    .doesNotContain(".application")
                    .doesNotContain(".infrastructure")
                    .doesNotContain(".presentation")
                    .doesNotContain(".persistence"));
  }

  private static Class<?>[] concatenate(Class<?>[] first, Class<?>[] second) {

    Class<?>[] result = Arrays.copyOf(first, first.length + second.length);

    System.arraycopy(second, 0, result, first.length, second.length);

    return result;
  }
}
