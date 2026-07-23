package com.shoestore.shared.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.shoestore.shared.domain.model.fixture.TestAggregateRoot;
import com.shoestore.shared.domain.model.fixture.TestAggregateStatus;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AggregateConstructionConventionTest {

  @Test
  void shouldCreateAggregateThroughFactoryMethod() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("Test aggregate");

    assertThat(aggregate).isNotNull();
    assertThat(aggregate.getName()).isEqualTo("Test aggregate");
    assertThat(aggregate.getStatus()).isEqualTo(TestAggregateStatus.ACTIVE);
    assertThat(aggregate.getChildren()).isEmpty();
  }

  @Test
  void shouldNormalizeRequiredNameDuringCreation() {
    TestAggregateRoot aggregate = TestAggregateRoot.create("  Test aggregate  ");

    assertThat(aggregate.getName()).isEqualTo("Test aggregate");
  }

  @Test
  void shouldRejectNullRequiredName() {
    assertThatNullPointerException()
        .isThrownBy(() -> TestAggregateRoot.create(null))
        .withMessage("aggregate name must not be null");
  }

  @Test
  void shouldRejectBlankRequiredName() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> TestAggregateRoot.create("   "))
        .withMessage("aggregate name must not be blank");
  }

  @Test
  void shouldDeclareJpaConstructorAsProtected() {
    Constructor<?> constructor =
        Arrays.stream(TestAggregateRoot.class.getDeclaredConstructors())
            .filter(candidate -> candidate.getParameterCount() == 0)
            .findFirst()
            .orElseThrow(() -> new AssertionError("JPA constructor was not declared"));

    assertThat(Modifier.isProtected(constructor.getModifiers())).isTrue();
  }

  @Test
  void shouldNotDeclarePublicConstructors() {
    Constructor<?>[] publicConstructors = TestAggregateRoot.class.getConstructors();

    assertThat(publicConstructors).isEmpty();
  }

  @Test
  void shouldExposePublicFactoryMethod() throws NoSuchMethodException {
    Method factoryMethod = TestAggregateRoot.class.getDeclaredMethod("create", String.class);

    assertThat(Modifier.isPublic(factoryMethod.getModifiers())).isTrue();

    assertThat(Modifier.isStatic(factoryMethod.getModifiers())).isTrue();

    assertThat(factoryMethod.getReturnType()).isEqualTo(TestAggregateRoot.class);
  }

  @Test
  void shouldNotDeclarePublicSetters() {
    boolean hasPublicSetter =
        Arrays.stream(TestAggregateRoot.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .anyMatch(
                name ->
                    name.startsWith("set")
                        && name.length() > 3
                        && Character.isUpperCase(name.charAt(3)));

    assertThat(hasPublicSetter).isFalse();
  }
}
