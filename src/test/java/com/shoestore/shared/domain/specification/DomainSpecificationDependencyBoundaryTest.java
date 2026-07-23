package com.shoestore.shared.domain.specification;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.domain.specification.fixture.TestProductCandidate;
import com.shoestore.shared.domain.specification.fixture.TestProductHasAvailableStockSpecification;
import com.shoestore.shared.domain.specification.fixture.TestProductIsActiveSpecification;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DomainSpecificationDependencyBoundaryTest {

  private static final List<String> FORBIDDEN_PACKAGE_PREFIXES =
      List.of(
          "org.springframework.",
          "jakarta.persistence.",
          "javax.persistence.",
          "org.hibernate.",
          "jakarta.servlet.",
          "javax.servlet.",
          "com.shoestore.modules.",
          "com.shoestore.shared.persistence.",
          "com.shoestore.shared.web.",
          "com.shoestore.shared.application.",
          "com.shoestore.shared.infrastructure.");

  private static final List<Class<?>> SPECIFICATION_TYPES =
      List.of(
          Specification.class,
          TestProductIsActiveSpecification.class,
          TestProductHasAvailableStockSpecification.class);

  @Test
  void specificationContractShouldNotExposeForbiddenDependencies() {
    assertNoForbiddenDependencySurface(Specification.class);
  }

  @Test
  void activeFixtureShouldNotExposeForbiddenDependencies() {
    assertNoForbiddenDependencySurface(TestProductIsActiveSpecification.class);
  }

  @Test
  void stockFixtureShouldNotExposeForbiddenDependencies() {
    assertNoForbiddenDependencySurface(TestProductHasAvailableStockSpecification.class);
  }

  @Test
  void fixtureSpecificationsShouldHaveNoInjectedCollaborators() {
    assertThat(TestProductIsActiveSpecification.class.getDeclaredFields()).isEmpty();

    assertThat(TestProductHasAvailableStockSpecification.class.getDeclaredFields()).isEmpty();
  }

  @Test
  void fixtureSpecificationsShouldUseNoArgumentConstruction() {
    assertHasOnlyNoArgumentConstructors(TestProductIsActiveSpecification.class);

    assertHasOnlyNoArgumentConstructors(TestProductHasAvailableStockSpecification.class);
  }

  @Test
  void fixtureSpecificationsShouldNotDeclareFrameworkAnnotations() {
    assertHasNoForbiddenAnnotations(TestProductIsActiveSpecification.class);

    assertHasNoForbiddenAnnotations(TestProductHasAvailableStockSpecification.class);
  }

  @Test
  void specificationContractShouldNotDeclareFrameworkAnnotations() {
    assertHasNoForbiddenAnnotations(Specification.class);
  }

  @Test
  void specificationCandidateShouldRemainADomainFixtureType() {
    Type specificationInterface =
        Arrays.stream(TestProductIsActiveSpecification.class.getGenericInterfaces())
            .filter(type -> type instanceof ParameterizedType)
            .findFirst()
            .orElseThrow();

    ParameterizedType parameterizedType = (ParameterizedType) specificationInterface;

    assertThat(parameterizedType.getRawType()).isEqualTo(Specification.class);

    assertThat(parameterizedType.getActualTypeArguments())
        .containsExactly(TestProductCandidate.class);
  }

  @Test
  void fixtureSpecificationsShouldDependOnlyOnApprovedProjectPackages() {
    assertOnlyApprovedProjectDependencies(TestProductIsActiveSpecification.class);

    assertOnlyApprovedProjectDependencies(TestProductHasAvailableStockSpecification.class);
  }

  @Test
  void allCurrentSpecificationTypesShouldRemainFrameworkIndependent() {
    SPECIFICATION_TYPES.forEach(
        DomainSpecificationDependencyBoundaryTest::assertNoForbiddenDependencySurface);
  }

  private static void assertNoForbiddenDependencySurface(Class<?> inspectedType) {

    Set<Class<?>> dependencyTypes = collectDependencySurface(inspectedType);

    List<Class<?>> forbiddenDependencies =
        dependencyTypes.stream()
            .filter(DomainSpecificationDependencyBoundaryTest::isForbiddenDependency)
            .toList();

    assertThat(forbiddenDependencies)
        .as("%s must not expose forbidden dependencies", inspectedType.getName())
        .isEmpty();
  }

  private static Set<Class<?>> collectDependencySurface(Class<?> inspectedType) {

    Set<Class<?>> dependencyTypes = new LinkedHashSet<>();

    addType(dependencyTypes, inspectedType.getGenericSuperclass());

    Arrays.stream(inspectedType.getGenericInterfaces())
        .forEach(type -> addType(dependencyTypes, type));

    Arrays.stream(inspectedType.getDeclaredFields())
        .map(Field::getGenericType)
        .forEach(type -> addType(dependencyTypes, type));

    Arrays.stream(inspectedType.getDeclaredConstructors())
        .flatMap(constructor -> Arrays.stream(constructor.getGenericParameterTypes()))
        .forEach(type -> addType(dependencyTypes, type));

    Arrays.stream(inspectedType.getDeclaredMethods())
        .filter(method -> !method.isSynthetic())
        .forEach(
            method -> {
              addType(dependencyTypes, method.getGenericReturnType());

              Arrays.stream(method.getGenericParameterTypes())
                  .forEach(type -> addType(dependencyTypes, type));

              Arrays.stream(method.getGenericExceptionTypes())
                  .forEach(type -> addType(dependencyTypes, type));
            });

    Arrays.stream(inspectedType.getDeclaredAnnotations())
        .map(Annotation::annotationType)
        .forEach(dependencyTypes::add);

    return dependencyTypes;
  }

  private static void addType(Set<Class<?>> collectedTypes, Type type) {

    if (type == null) {
      return;
    }

    if (type instanceof Class<?> classType) {
      addClass(collectedTypes, classType);

      return;
    }

    if (type instanceof ParameterizedType parameterizedType) {
      addType(collectedTypes, parameterizedType.getRawType());

      Arrays.stream(parameterizedType.getActualTypeArguments())
          .forEach(actualType -> addType(collectedTypes, actualType));

      return;
    }

    if (type instanceof WildcardType wildcardType) {
      Arrays.stream(wildcardType.getUpperBounds()).forEach(bound -> addType(collectedTypes, bound));

      Arrays.stream(wildcardType.getLowerBounds()).forEach(bound -> addType(collectedTypes, bound));
    }
  }

  private static void addClass(Set<Class<?>> collectedTypes, Class<?> classType) {

    if (classType.isArray()) {
      addClass(collectedTypes, classType.getComponentType());

      return;
    }

    if (!classType.isPrimitive()) {
      collectedTypes.add(classType);
    }
  }

  private static boolean isForbiddenDependency(Class<?> dependencyType) {

    String typeName = dependencyType.getName();

    return FORBIDDEN_PACKAGE_PREFIXES.stream().anyMatch(typeName::startsWith);
  }

  private static void assertHasOnlyNoArgumentConstructors(Class<?> specificationType) {

    Constructor<?>[] constructors = specificationType.getDeclaredConstructors();

    assertThat(constructors).isNotEmpty();

    assertThat(Arrays.stream(constructors).mapToInt(Constructor::getParameterCount).boxed())
        .as("%s must not require injected collaborators", specificationType.getSimpleName())
        .containsOnly(0);
  }

  private static void assertHasNoForbiddenAnnotations(Class<?> inspectedType) {

    List<String> forbiddenAnnotations =
        Arrays.stream(inspectedType.getDeclaredAnnotations())
            .map(annotation -> annotation.annotationType())
            .filter(annotationType -> isForbiddenDependency(annotationType))
            .map(Class::getName)
            .toList();

    assertThat(forbiddenAnnotations)
        .as("%s must not declare framework annotations", inspectedType.getName())
        .isEmpty();
  }

  private static void assertOnlyApprovedProjectDependencies(Class<?> inspectedType) {

    Set<Class<?>> dependencyTypes = collectDependencySurface(inspectedType);

    List<Class<?>> unapprovedProjectDependencies =
        dependencyTypes.stream()
            .filter(type -> type.getName().startsWith("com.shoestore."))
            .filter(type -> !isApprovedProjectDependency(type))
            .toList();

    assertThat(unapprovedProjectDependencies)
        .as(
            "%s must depend only on approved Domain Specification packages",
            inspectedType.getName())
        .isEmpty();
  }

  private static boolean isApprovedProjectDependency(Class<?> dependencyType) {

    String typeName = dependencyType.getName();

    return typeName.startsWith("com.shoestore.shared.domain.specification.");
  }
}
