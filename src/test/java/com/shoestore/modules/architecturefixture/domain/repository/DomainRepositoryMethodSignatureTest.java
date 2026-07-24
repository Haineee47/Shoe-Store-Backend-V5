package com.shoestore.modules.architecturefixture.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.domain.model.TestRepositoryAggregate;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryAggregateId;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryLookupKey;
import com.shoestore.shared.domain.model.AggregateRoot;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainRepositoryMethodSignatureTest {

  private static final String DOMAIN_PACKAGE_PREFIX =
      "com.shoestore.modules.architecturefixture.domain.";

  private static final Set<String> FORBIDDEN_TYPE_PREFIXES =
      Set.of(
          "jakarta.persistence.",
          "javax.persistence.",
          "org.hibernate.",
          "org.springframework.",
          "com.shoestore.shared.persistence.");

  private static final Set<String> FORBIDDEN_GENERIC_CRUD_METHOD_NAMES =
      Set.of(
          "findAll",
          "saveAll",
          "delete",
          "deleteById",
          "deleteAll",
          "count",
          "flush",
          "saveAndFlush",
          "getReferenceById");

  @Test
  void repositoryShouldNotDeclareGenericTypeParameters() {
    assertThat(TestAggregateRepository.class.getTypeParameters())
        .as("Domain Repository must not become a generic CRUD abstraction")
        .isEmpty();
  }

  @Test
  void repositoryMethodsShouldNotDeclareGenericTypeParameters() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .allSatisfy(
            method ->
                assertThat(method.getTypeParameters())
                    .as(
                        "Method %s must not introduce generic repository behavior",
                        method.getName())
                    .isEmpty());
  }

  @Test
  void repositoryShouldExposeOnlyApprovedBusinessSpecificMethods() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .extracting(Method::getName)
        .containsExactlyInAnyOrder(
            "findById",
            "findByLookupKey",
            "existsByLookupKey",
            "save");
  }

  @Test
  void repositoryShouldNotExposeGenericCrudMethods() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .extracting(Method::getName)
        .doesNotContainAnyElementsOf(FORBIDDEN_GENERIC_CRUD_METHOD_NAMES);
  }

  @Test
  void findByIdShouldUseTypedIdentityAndReturnOptionalAggregate()
      throws NoSuchMethodException {

    Method method =
        TestAggregateRepository.class.getDeclaredMethod(
            "findById",
            TestRepositoryAggregateId.class);

    assertThat(method.getParameterTypes())
        .containsExactly(TestRepositoryAggregateId.class);

    assertThat(method.getGenericReturnType())
        .isEqualTo(
            parameterizedType(
                Optional.class,
                TestRepositoryAggregate.class));
  }

  @Test
  void findByLookupKeyShouldUseDomainValueObjectAndReturnOptionalAggregate()
      throws NoSuchMethodException {

    Method method =
        TestAggregateRepository.class.getDeclaredMethod(
            "findByLookupKey",
            TestRepositoryLookupKey.class);

    assertThat(method.getParameterTypes())
        .containsExactly(TestRepositoryLookupKey.class);

    assertThat(method.getGenericReturnType())
        .isEqualTo(
            parameterizedType(
                Optional.class,
                TestRepositoryAggregate.class));
  }

  @Test
  void existsByLookupKeyShouldUseDomainValueObjectAndReturnPrimitiveBoolean()
      throws NoSuchMethodException {

    Method method =
        TestAggregateRepository.class.getDeclaredMethod(
            "existsByLookupKey",
            TestRepositoryLookupKey.class);

    assertThat(method.getParameterTypes())
        .containsExactly(TestRepositoryLookupKey.class);

    assertThat(method.getReturnType()).isEqualTo(boolean.class);
  }

  @Test
  void saveShouldAcceptAndReturnTheAggregateRoot()
      throws NoSuchMethodException {

    Method method =
        TestAggregateRepository.class.getDeclaredMethod(
            "save",
            TestRepositoryAggregate.class);

    assertThat(method.getParameterTypes())
        .containsExactly(TestRepositoryAggregate.class);

    assertThat(method.getReturnType())
        .isEqualTo(TestRepositoryAggregate.class);

    assertThat(AggregateRoot.class)
        .isAssignableFrom(method.getParameterTypes()[0]);

    assertThat(AggregateRoot.class)
        .isAssignableFrom(method.getReturnType());
  }

  @Test
  void repositoryShouldNotUseRawIdentityOrLookupRepresentations() {
    List<Class<?>> exposedRawTypes =
        Arrays.stream(TestAggregateRepository.class.getDeclaredMethods())
            .flatMap(
                method ->
                    Arrays.stream(method.getParameterTypes()))
            .toList();

    assertThat(exposedRawTypes)
        .doesNotContain(
            UUID.class,
            String.class,
            Object.class);
  }

  @Test
  void repositoryGenericSignaturesShouldContainNoForbiddenFrameworkTypes() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .allSatisfy(
            method -> {
              assertAllowedType(
                  method.getGenericReturnType(),
                  method.getName() + " return type");

              Arrays.stream(method.getGenericParameterTypes())
                  .forEach(
                      parameterType ->
                          assertAllowedType(
                              parameterType,
                              method.getName() + " parameter type"));
            });
  }

  @Test
  void repositoryShouldUseOnlyDomainTypesJavaTypesOrPrimitives() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .allSatisfy(
            method -> {
              assertDomainRepositoryType(
                  method.getGenericReturnType(),
                  method.getName() + " return type");

              Arrays.stream(method.getGenericParameterTypes())
                  .forEach(
                      parameterType ->
                          assertDomainRepositoryType(
                              parameterType,
                              method.getName() + " parameter type"));
            });
  }

  private void assertAllowedType(Type type, String location) {
    assertThat(containsForbiddenType(type))
        .as("%s must not reference framework or persistence type: %s", location, type)
        .isFalse();
  }

  private void assertDomainRepositoryType(Type type, String location) {
    assertThat(isAllowedDomainRepositoryType(type))
        .as("%s must use only Java, primitive or Domain types: %s", location, type)
        .isTrue();
  }

  private boolean containsForbiddenType(Type type) {
    if (type instanceof Class<?> typeClass) {
      return FORBIDDEN_TYPE_PREFIXES.stream()
          .anyMatch(typeClass.getName()::startsWith);
    }

    if (type instanceof ParameterizedType parameterizedType) {
      if (containsForbiddenType(parameterizedType.getRawType())) {
        return true;
      }

      return Arrays.stream(parameterizedType.getActualTypeArguments())
          .anyMatch(this::containsForbiddenType);
    }

    if (type instanceof GenericArrayType genericArrayType) {
      return containsForbiddenType(
          genericArrayType.getGenericComponentType());
    }

    if (type instanceof WildcardType wildcardType) {
      return Arrays.stream(wildcardType.getUpperBounds())
              .anyMatch(this::containsForbiddenType)
          || Arrays.stream(wildcardType.getLowerBounds())
              .anyMatch(this::containsForbiddenType);
    }

    if (type instanceof TypeVariable<?> typeVariable) {
      return Arrays.stream(typeVariable.getBounds())
          .anyMatch(this::containsForbiddenType);
    }

    return false;
  }

  private boolean isAllowedDomainRepositoryType(Type type) {
    if (type instanceof Class<?> typeClass) {
      if (typeClass.isPrimitive()) {
        return true;
      }

      String typeName = typeClass.getName();

      return typeName.startsWith("java.")
          || typeName.startsWith(DOMAIN_PACKAGE_PREFIX);
    }

    if (type instanceof ParameterizedType parameterizedType) {
      return isAllowedDomainRepositoryType(parameterizedType.getRawType())
          && Arrays.stream(parameterizedType.getActualTypeArguments())
              .allMatch(this::isAllowedDomainRepositoryType);
    }

    if (type instanceof GenericArrayType genericArrayType) {
      return isAllowedDomainRepositoryType(
          genericArrayType.getGenericComponentType());
    }

    if (type instanceof WildcardType wildcardType) {
      return Arrays.stream(wildcardType.getUpperBounds())
              .allMatch(this::isAllowedDomainRepositoryType)
          && Arrays.stream(wildcardType.getLowerBounds())
              .allMatch(this::isAllowedDomainRepositoryType);
    }

    if (type instanceof TypeVariable<?> typeVariable) {
      return Arrays.stream(typeVariable.getBounds())
          .allMatch(this::isAllowedDomainRepositoryType);
    }

    return false;
  }

  private ParameterizedType parameterizedType(
      Class<?> rawType,
      Class<?> typeArgument) {

    return new ExpectedParameterizedType(
        rawType,
        new Type[] {typeArgument});
  }

  private record ExpectedParameterizedType(
    Type rawType,
    Type[] actualTypeArguments)
    implements ParameterizedType {

  private ExpectedParameterizedType {
    actualTypeArguments = actualTypeArguments.clone();
  }

  @Override
  public Type[] getActualTypeArguments() {
    return actualTypeArguments.clone();
  }

  @Override
  public Type getRawType() {
    return rawType;
  }

  @Override
  public Type getOwnerType() {
    return null;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ParameterizedType parameterizedType)) {
      return false;
    }

    return rawType.equals(parameterizedType.getRawType())
        && Arrays.equals(
            actualTypeArguments,
            parameterizedType.getActualTypeArguments())
        && parameterizedType.getOwnerType() == null;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(actualTypeArguments)
        ^ rawType.hashCode();
  }

  @Override
  public String getTypeName() {
    return rawType.getTypeName()
        + "<"
        + Arrays.stream(actualTypeArguments)
            .map(Type::getTypeName)
            .reduce((left, right) -> left + ", " + right)
            .orElse("")
        + ">";
  }
}
}
