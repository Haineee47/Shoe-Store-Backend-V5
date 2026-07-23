package com.shoestore.shared.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.domain.model.fixture.TestChildEntity;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ChildEntityOwnershipConventionTest {

  @Test
  void shouldNotExposePublicConstructorsForChildEntity() {
    Constructor<?>[] publicConstructors = TestChildEntity.class.getConstructors();

    assertThat(publicConstructors).isEmpty();
  }

  @Test
  void shouldKeepChildFactoryPackagePrivate() throws NoSuchMethodException {
    Method factoryMethod = TestChildEntity.class.getDeclaredMethod("create", String.class);

    assertThat(Modifier.isPublic(factoryMethod.getModifiers())).isFalse();

    assertThat(Modifier.isProtected(factoryMethod.getModifiers())).isFalse();

    assertThat(Modifier.isPrivate(factoryMethod.getModifiers())).isFalse();

    assertThat(Modifier.isStatic(factoryMethod.getModifiers())).isTrue();
  }

  @Test
  void shouldKeepChildMutationMethodsNonPublic() throws NoSuchMethodException {

    Method renameMethod = TestChildEntity.class.getDeclaredMethod("rename", String.class);

    assertThat(Modifier.isPublic(renameMethod.getModifiers())).isFalse();
  }

  @Test
  void shouldNotDeclarePublicSettersOnChildEntity() {
    boolean hasPublicSetter =
        Arrays.stream(TestChildEntity.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .anyMatch(
                name ->
                    name.startsWith("set")
                        && name.length() > 3
                        && Character.isUpperCase(name.charAt(3)));

    assertThat(hasPublicSetter).isFalse();
  }

  @Test
  void shouldNotMarkChildEntityAsAggregateRoot() {
    assertThat(AggregateRoot.class.isAssignableFrom(TestChildEntity.class)).isFalse();
  }
}
