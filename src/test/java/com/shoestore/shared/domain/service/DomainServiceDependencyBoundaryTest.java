package com.shoestore.shared.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.domain.service.fixture.TestAllocationPolicy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class DomainServiceDependencyBoundaryTest {

  private static final Set<String> PROHIBITED_PACKAGE_PREFIXES =
      Set.of(
          "org.springframework.",
          "jakarta.persistence.",
          "jakarta.servlet.",
          "org.hibernate.",
          "java.sql.",
          "javax.sql.",
          "org.slf4j.");

  @Test
  void shouldNotDeclareFrameworkAnnotations() {
    String[] annotationTypeNames =
        Arrays.stream(TestAllocationPolicy.class.getDeclaredAnnotations())
            .map(Annotation::annotationType)
            .map(Class::getName)
            .toArray(String[]::new);

    assertThat(annotationTypeNames).isEmpty();
  }

  @Test
  void shouldNotDeclareDependencyFields() {
    assertThat(TestAllocationPolicy.class.getDeclaredFields()).isEmpty();
  }

  @Test
  void shouldUseOnlyAllowedConstructorDependencies() {
    Class<?>[] constructorParameterTypes =
        Arrays.stream(TestAllocationPolicy.class.getDeclaredConstructors())
            .map(Constructor::getParameterTypes)
            .flatMap(Arrays::stream)
            .toArray(Class<?>[]::new);

    assertThat(constructorParameterTypes).isEmpty();
  }

  @Test
  void shouldNotExposeProhibitedTypesInMethodSignatures() {
    String[] prohibitedTypeNames =
        Arrays.stream(TestAllocationPolicy.class.getDeclaredMethods())
            .flatMap(this::signatureTypes)
            .map(Class::getName)
            .filter(this::isProhibitedPackage)
            .toArray(String[]::new);

    assertThat(prohibitedTypeNames)
        .as("Domain Service fixture must not expose framework or infrastructure types")
        .isEmpty();
  }

  private Stream<Class<?>> signatureTypes(Method method) {
    return Stream.concat(
        Stream.of(method.getReturnType()), Arrays.stream(method.getParameterTypes()));
  }

  private boolean isProhibitedPackage(String typeName) {
    return PROHIBITED_PACKAGE_PREFIXES.stream().anyMatch(typeName::startsWith);
  }
}
