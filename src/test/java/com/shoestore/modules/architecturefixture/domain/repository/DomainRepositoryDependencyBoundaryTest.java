package com.shoestore.modules.architecturefixture.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DomainRepositoryDependencyBoundaryTest {

  private static final Set<String> FORBIDDEN_PACKAGE_PREFIXES =
      Set.of(
          "jakarta.persistence.",
          "javax.persistence.",
          "org.hibernate.",
          "org.springframework.",
          "com.shoestore.shared.persistence.");

  private static final Set<String> FORBIDDEN_PACKAGE_SEGMENTS =
      Set.of(".application.", ".infrastructure.", ".web.", ".controller.", ".dto.");

  private static final Set<String> FORBIDDEN_SIMPLE_TYPE_NAMES =
      Set.of(
          "Page",
          "Pageable",
          "Sort",
          "Specification",
          "EntityManager",
          "EntityManagerFactory",
          "JpaRepository",
          "CrudRepository",
          "PagingAndSortingRepository");

  @Test
  void domainRepositoryShouldRemainAnInterface() {
    assertThat(TestAggregateRepository.class.isInterface()).isTrue();
  }

  @Test
  void domainRepositoryShouldDeclareNoFrameworkAnnotations() {
    assertThat(TestAggregateRepository.class.getDeclaredAnnotations())
        .allSatisfy(
            annotation -> assertAllowedAnnotation(annotation, TestAggregateRepository.class));
  }

  @Test
  void domainRepositoryMethodsShouldDeclareNoFrameworkAnnotations() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .allSatisfy(
            method -> {
              assertThat(method.getDeclaredAnnotations())
                  .allSatisfy(annotation -> assertAllowedAnnotation(annotation, method));

              Arrays.stream(method.getParameterAnnotations())
                  .flatMap(Arrays::stream)
                  .forEach(annotation -> assertAllowedAnnotation(annotation, method));
            });
  }

  @Test
  void domainRepositoryShouldNotExtendForbiddenInterfaces() {
    assertThat(TestAggregateRepository.class.getGenericInterfaces())
        .allSatisfy(
            dependency -> assertAllowedDependency(dependency, "repository parent interface"));
  }

  @Test
  void domainRepositoryMethodParametersShouldRespectDependencyBoundary() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .allSatisfy(
            method ->
                Arrays.stream(method.getGenericParameterTypes())
                    .forEach(
                        parameterType ->
                            assertAllowedDependency(
                                parameterType, method.getName() + " parameter")));
  }

  @Test
  void domainRepositoryReturnTypesShouldRespectDependencyBoundary() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .allSatisfy(
            method ->
                assertAllowedDependency(
                    method.getGenericReturnType(), method.getName() + " return type"));
  }

  @Test
  void domainRepositoryExceptionsShouldRespectDependencyBoundary() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .allSatisfy(
            method ->
                Arrays.stream(method.getGenericExceptionTypes())
                    .forEach(
                        exceptionType ->
                            assertAllowedDependency(
                                exceptionType, method.getName() + " declared exception")));
  }

  @Test
  void domainRepositoryShouldNotExposeForbiddenKnownTypes() {
    assertThat(TestAggregateRepository.class.getDeclaredMethods())
        .allSatisfy(
            method -> {
              assertThat(containsForbiddenSimpleType(method.getGenericReturnType()))
                  .as(
                      "%s return type must not expose a persistence or framework abstraction",
                      method.getName())
                  .isFalse();

              Arrays.stream(method.getGenericParameterTypes())
                  .forEach(
                      parameterType ->
                          assertThat(containsForbiddenSimpleType(parameterType))
                              .as(
                                  "%s parameter must not expose a persistence or framework abstraction",
                                  method.getName())
                              .isFalse());
            });
  }

  private void assertAllowedAnnotation(Annotation annotation, AnnotatedElement owner) {

    String annotationName = annotation.annotationType().getName();

    assertThat(isForbiddenClassName(annotationName))
        .as("%s must not declare framework annotation %s", owner, annotationName)
        .isFalse();
  }

  private void assertAllowedDependency(Type dependency, String location) {

    assertThat(containsForbiddenDependency(dependency))
        .as("%s must not reference forbidden dependency %s", location, dependency.getTypeName())
        .isFalse();
  }

  private boolean containsForbiddenDependency(Type type) {
    if (type instanceof Class<?> typeClass) {
      if (typeClass.isArray()) {
        return containsForbiddenDependency(typeClass.getComponentType());
      }

      return isForbiddenClassName(typeClass.getName());
    }

    if (type instanceof ParameterizedType parameterizedType) {
      if (containsForbiddenDependency(parameterizedType.getRawType())) {

        return true;
      }

      if (parameterizedType.getOwnerType() != null
          && containsForbiddenDependency(parameterizedType.getOwnerType())) {

        return true;
      }

      return Arrays.stream(parameterizedType.getActualTypeArguments())
          .anyMatch(this::containsForbiddenDependency);
    }

    if (type instanceof GenericArrayType genericArrayType) {
      return containsForbiddenDependency(genericArrayType.getGenericComponentType());
    }

    if (type instanceof WildcardType wildcardType) {
      return Arrays.stream(wildcardType.getUpperBounds())
              .anyMatch(this::containsForbiddenDependency)
          || Arrays.stream(wildcardType.getLowerBounds())
              .anyMatch(this::containsForbiddenDependency);
    }

    if (type instanceof TypeVariable<?> typeVariable) {
      return Arrays.stream(typeVariable.getBounds()).anyMatch(this::containsForbiddenDependency);
    }

    return false;
  }

  private boolean containsForbiddenSimpleType(Type type) {
    if (type instanceof Class<?> typeClass) {
      if (typeClass.isArray()) {
        return containsForbiddenSimpleType(typeClass.getComponentType());
      }

      return FORBIDDEN_SIMPLE_TYPE_NAMES.contains(typeClass.getSimpleName());
    }

    if (type instanceof ParameterizedType parameterizedType) {
      if (containsForbiddenSimpleType(parameterizedType.getRawType())) {

        return true;
      }

      return Arrays.stream(parameterizedType.getActualTypeArguments())
          .anyMatch(this::containsForbiddenSimpleType);
    }

    if (type instanceof GenericArrayType genericArrayType) {
      return containsForbiddenSimpleType(genericArrayType.getGenericComponentType());
    }

    if (type instanceof WildcardType wildcardType) {
      return Arrays.stream(wildcardType.getUpperBounds())
              .anyMatch(this::containsForbiddenSimpleType)
          || Arrays.stream(wildcardType.getLowerBounds())
              .anyMatch(this::containsForbiddenSimpleType);
    }

    if (type instanceof TypeVariable<?> typeVariable) {
      return Arrays.stream(typeVariable.getBounds()).anyMatch(this::containsForbiddenSimpleType);
    }

    return false;
  }

  private boolean isForbiddenClassName(String className) {

    return FORBIDDEN_PACKAGE_PREFIXES.stream().anyMatch(className::startsWith)
        || FORBIDDEN_PACKAGE_SEGMENTS.stream().anyMatch(className::contains);
  }
}
