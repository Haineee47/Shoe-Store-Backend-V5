package com.shoestore.modules.architecturefixture.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.domain.model.TestRepositoryAggregate;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryAggregateId;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestRepositoryLookupKey;
import com.shoestore.shared.domain.model.AggregateRoot;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class DomainRepositoryBoundaryTest {

  private static final Set<String> FORBIDDEN_TYPE_PREFIXES =
      Set.of(
          "jakarta.persistence.",
          "javax.persistence.",
          "org.hibernate.",
          "org.springframework.",
          "com.shoestore.shared.persistence.");

  @Test
  void aggregateFixtureShouldImplementAggregateRootContract() {
    assertThat(AggregateRoot.class)
        .isAssignableFrom(TestRepositoryAggregate.class);

    assertThat(TestRepositoryAggregate.class.isInterface()).isFalse();
    assertThat(Modifier.isFinal(TestRepositoryAggregate.class.getModifiers())).isTrue();
  }

  @Test
  void domainRepositoryShouldBeAPlainJavaInterface() {
    assertThat(TestAggregateRepository.class.isInterface()).isTrue();

    assertThat(TestAggregateRepository.class.getInterfaces()).isEmpty();
  }

  @Test
  void domainRepositoryShouldBelongToDomainRepositoryPackage() {
    assertThat(TestAggregateRepository.class.getPackageName())
        .endsWith(".domain.repository");
  }

  @Test
  void domainRepositoryShouldUseAggregateRootAndTypedDomainValues() throws NoSuchMethodException {
    Method findById =
        TestAggregateRepository.class.getDeclaredMethod(
            "findById",
            TestRepositoryAggregateId.class);

    Method findByLookupKey =
        TestAggregateRepository.class.getDeclaredMethod(
            "findByLookupKey",
            TestRepositoryLookupKey.class);

    Method existsByLookupKey =
        TestAggregateRepository.class.getDeclaredMethod(
            "existsByLookupKey",
            TestRepositoryLookupKey.class);

    Method save =
        TestAggregateRepository.class.getDeclaredMethod(
            "save",
            TestRepositoryAggregate.class);

    assertThat(findById.getReturnType()).isEqualTo(Optional.class);
    assertThat(findById.getGenericReturnType().getTypeName())
        .contains(TestRepositoryAggregate.class.getName());

    assertThat(findByLookupKey.getReturnType()).isEqualTo(Optional.class);
    assertThat(findByLookupKey.getGenericReturnType().getTypeName())
        .contains(TestRepositoryAggregate.class.getName());

    assertThat(existsByLookupKey.getReturnType()).isEqualTo(boolean.class);
    assertThat(save.getReturnType()).isEqualTo(TestRepositoryAggregate.class);
  }

  @Test
  void domainRepositoryShouldExposeOnlyDomainOrJavaTypes() {
    Stream<Class<?>> exposedTypes =
        Arrays.stream(TestAggregateRepository.class.getDeclaredMethods())
            .flatMap(
                method ->
                    Stream.concat(
                        Stream.of(method.getReturnType()),
                        Arrays.stream(method.getParameterTypes())));

    assertThat(exposedTypes)
        .allSatisfy(
            type ->
                assertThat(isAllowedDomainRepositoryType(type))
                    .as("Repository type %s must be a Java or Domain type", type.getName())
                    .isTrue());
  }

  @Test
  void domainRepositoryShouldNotReferenceFrameworkOrPersistenceTypes() {
    Stream<String> exposedTypeNames =
        Arrays.stream(TestAggregateRepository.class.getDeclaredMethods())
            .flatMap(
                method ->
                    Stream.concat(
                        Stream.of(method.getGenericReturnType().getTypeName()),
                        Arrays.stream(method.getGenericParameterTypes())
                            .map(type -> type.getTypeName())));

    assertThat(exposedTypeNames)
        .noneMatch(this::containsForbiddenType);
  }

  private boolean isAllowedDomainRepositoryType(Class<?> type) {
    if (type.isPrimitive()) {
      return true;
    }

    String typeName = type.getName();

    return typeName.startsWith("java.")
        || typeName.startsWith("com.shoestore.modules.architecturefixture.domain.");
  }

  private boolean containsForbiddenType(String typeName) {
    return FORBIDDEN_TYPE_PREFIXES.stream().anyMatch(typeName::contains);
  }
}
