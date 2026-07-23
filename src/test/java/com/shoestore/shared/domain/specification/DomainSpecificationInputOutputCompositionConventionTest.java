package com.shoestore.shared.domain.specification;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.domain.specification.fixture.TestProductCandidate;
import com.shoestore.shared.domain.specification.fixture.TestProductHasAvailableStockSpecification;
import com.shoestore.shared.domain.specification.fixture.TestProductIsActiveSpecification;
import com.shoestore.shared.domain.specification.fixture.TestProductStatus;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DomainSpecificationInputOutputCompositionConventionTest {

  @Test
  void specificationShouldBeAnInterface() {
    assertThat(Specification.class.isInterface()).isTrue();
  }

  @Test
  void specificationShouldBeAnnotatedAsFunctionalInterface() {
    assertThat(Specification.class.isAnnotationPresent(FunctionalInterface.class)).isTrue();
  }

  @Test
  void specificationShouldDeclareExactlyOneAbstractMethod() {
    long abstractMethodCount =
        Arrays.stream(Specification.class.getDeclaredMethods())
            .filter(method -> Modifier.isAbstract(method.getModifiers()))
            .count();

    assertThat(abstractMethodCount).isEqualTo(1);
  }

  @Test
  void canonicalAbstractMethodShouldBeIsSatisfiedBy() {
    Method abstractMethod =
        Arrays.stream(Specification.class.getDeclaredMethods())
            .filter(method -> Modifier.isAbstract(method.getModifiers()))
            .findFirst()
            .orElseThrow();

    assertThat(abstractMethod.getName()).isEqualTo("isSatisfiedBy");
  }

  @Test
  void isSatisfiedByShouldAcceptExactlyOneCandidate() {
    Method method = declaredMethod("isSatisfiedBy", Object.class);

    assertThat(method.getParameterCount()).isEqualTo(1);
  }

  @Test
  void isSatisfiedByShouldReturnPrimitiveBoolean() {
    Method method = declaredMethod("isSatisfiedBy", Object.class);

    assertThat(method.getReturnType()).isEqualTo(boolean.class);
  }

  @Test
  void isSatisfiedByShouldNotReturnBoxedBoolean() {
    Method method = declaredMethod("isSatisfiedBy", Object.class);

    assertThat(method.getReturnType()).isNotEqualTo(Boolean.class);
  }

  @Test
  void andShouldBeADefaultMethod() {
    Method method = declaredMethod("and", Specification.class);

    assertThat(method.isDefault()).isTrue();
  }

  @Test
  void orShouldBeADefaultMethod() {
    Method method = declaredMethod("or", Specification.class);

    assertThat(method.isDefault()).isTrue();
  }

  @Test
  void notShouldBeADefaultMethod() {
    Method method = declaredMethod("not");

    assertThat(method.isDefault()).isTrue();
  }

  @Test
  void andShouldAcceptExactlyOneSpecificationOperand() {
    Method method = declaredMethod("and", Specification.class);

    assertThat(method.getParameterCount()).isEqualTo(1);

    assertThat(method.getParameterTypes()[0]).isEqualTo(Specification.class);
  }

  @Test
  void orShouldAcceptExactlyOneSpecificationOperand() {
    Method method = declaredMethod("or", Specification.class);

    assertThat(method.getParameterCount()).isEqualTo(1);

    assertThat(method.getParameterTypes()[0]).isEqualTo(Specification.class);
  }

  @Test
  void notShouldAcceptNoOperand() {
    Method method = declaredMethod("not");

    assertThat(method.getParameterCount()).isZero();
  }

  @Test
  void andShouldReturnSpecification() {
    Method method = declaredMethod("and", Specification.class);

    assertThat(method.getReturnType()).isEqualTo(Specification.class);
  }

  @Test
  void orShouldReturnSpecification() {
    Method method = declaredMethod("or", Specification.class);

    assertThat(method.getReturnType()).isEqualTo(Specification.class);
  }

  @Test
  void notShouldReturnSpecification() {
    Method method = declaredMethod("not");

    assertThat(method.getReturnType()).isEqualTo(Specification.class);
  }

  @Test
  void andShouldReturnSpecificationOfSameCandidateType() {
    Method method = declaredMethod("and", Specification.class);

    Type genericReturnType = method.getGenericReturnType();

    assertThat(genericReturnType).isInstanceOf(ParameterizedType.class);

    ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;

    assertThat(parameterizedType.getRawType()).isEqualTo(Specification.class);

    assertThat(parameterizedType.getActualTypeArguments())
        .containsExactly(Specification.class.getTypeParameters()[0]);
  }

  @Test
  void orShouldReturnSpecificationOfSameCandidateType() {
    Method method = declaredMethod("or", Specification.class);

    Type genericReturnType = method.getGenericReturnType();

    assertThat(genericReturnType).isInstanceOf(ParameterizedType.class);

    ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;

    assertThat(parameterizedType.getRawType()).isEqualTo(Specification.class);

    assertThat(parameterizedType.getActualTypeArguments())
        .containsExactly(Specification.class.getTypeParameters()[0]);
  }

  @Test
  void notShouldReturnSpecificationOfSameCandidateType() {
    Method method = declaredMethod("not");

    Type genericReturnType = method.getGenericReturnType();

    assertThat(genericReturnType).isInstanceOf(ParameterizedType.class);

    ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;

    assertThat(parameterizedType.getRawType()).isEqualTo(Specification.class);

    assertThat(parameterizedType.getActualTypeArguments())
        .containsExactly(Specification.class.getTypeParameters()[0]);
  }

  @Test
  void andOperandShouldUseContravariantSpecificationType() {
    Method method = declaredMethod("and", Specification.class);

    Type genericParameterType = method.getGenericParameterTypes()[0];

    assertThat(genericParameterType.getTypeName())
        .isEqualTo("com.shoestore.shared.domain.specification." + "Specification<? super T>");
  }

  @Test
  void orOperandShouldUseContravariantSpecificationType() {
    Method method = declaredMethod("or", Specification.class);

    Type genericParameterType = method.getGenericParameterTypes()[0];

    assertThat(genericParameterType.getTypeName())
        .isEqualTo("com.shoestore.shared.domain.specification." + "Specification<? super T>");
  }

  @Test
  void specificationShouldExposeOnlyBooleanEvaluationAndCompositionOperations() {
    assertThat(
            Arrays.stream(Specification.class.getDeclaredMethods())
                .filter(method -> !method.isSynthetic())
                .map(Method::getName))
        .containsExactlyInAnyOrder("isSatisfiedBy", "and", "or", "not");
  }

  @Test
  void fixtureSpecificationsShouldImplementTypedSpecificationContract() {
    assertTypedSpecificationCandidate(
        TestProductIsActiveSpecification.class, TestProductCandidate.class);

    assertTypedSpecificationCandidate(
        TestProductHasAvailableStockSpecification.class, TestProductCandidate.class);
  }

  @Test
  void fixtureCandidateShouldBeAnImmutableRecord() {
    assertThat(TestProductCandidate.class.isRecord()).isTrue();

    assertThat(Modifier.isFinal(TestProductCandidate.class.getModifiers())).isTrue();
  }

  @Test
  void fixtureCandidateShouldUseTypedDomainFields() {
    Class<?>[] componentTypes =
        Arrays.stream(TestProductCandidate.class.getRecordComponents())
            .map(component -> component.getType())
            .toArray(Class<?>[]::new);

    assertThat(componentTypes).containsExactly(TestProductStatus.class, int.class);
  }

  @Test
  void composedSpecificationShouldRemainStronglyTyped() {
    Specification<TestProductCandidate> activeSpecification =
        new TestProductIsActiveSpecification();

    Specification<TestProductCandidate> stockSpecification =
        new TestProductHasAvailableStockSpecification();

    Specification<TestProductCandidate> composedSpecification =
        activeSpecification.and(stockSpecification).or(activeSpecification.not());

    TestProductCandidate candidate = new TestProductCandidate(TestProductStatus.ACTIVE, 1);

    assertThat(composedSpecification.isSatisfiedBy(candidate)).isTrue();
  }

  @Test
  void subtypeSpecificationShouldComposeWithSupertypeSpecification() {
    Specification<TestDomainCandidate> generalCandidateSpecification =
        candidate -> candidate != null;

    Specification<TestSpecializedCandidate> specializedSpecification =
        candidate -> candidate != null && candidate.enabled();

    Specification<TestSpecializedCandidate> composedSpecification =
        specializedSpecification.and(generalCandidateSpecification);

    TestSpecializedCandidate candidate = new TestSpecializedCandidate(true);

    assertThat(composedSpecification.isSatisfiedBy(candidate)).isTrue();
  }

  private static Method declaredMethod(String methodName, Class<?>... parameterTypes) {

    try {
      return Specification.class.getDeclaredMethod(methodName, parameterTypes);
    } catch (NoSuchMethodException exception) {
      throw new AssertionError(
          "Expected Specification method was not found: " + methodName, exception);
    }
  }

  private static void assertTypedSpecificationCandidate(
      Class<?> specificationType, Class<?> expectedCandidateType) {

    Type specificationInterface =
        Arrays.stream(specificationType.getGenericInterfaces())
            .filter(type -> type instanceof ParameterizedType)
            .findFirst()
            .orElseThrow();

    ParameterizedType parameterizedType = (ParameterizedType) specificationInterface;

    assertThat(parameterizedType.getRawType()).isEqualTo(Specification.class);

    assertThat(parameterizedType.getActualTypeArguments()).containsExactly(expectedCandidateType);
  }

  private interface TestDomainCandidate {}

  private record TestSpecializedCandidate(boolean enabled) implements TestDomainCandidate {}
}
