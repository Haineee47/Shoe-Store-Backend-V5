package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.command.ActivateTestProductCommand;
import com.shoestore.modules.architecturefixture.application.command.CreateTestProductWithIdCommand;
import com.shoestore.modules.architecturefixture.application.command.DeactivateTestProductCommand;
import com.shoestore.modules.architecturefixture.application.query.FindTestProductByIdQuery;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationInputConventionTest {

  private static final List<Class<?>> APPLICATION_INPUT_TYPES =
      List.of(
          ActivateTestProductCommand.class,
          CreateTestProductWithIdCommand.class,
          DeactivateTestProductCommand.class,
          FindTestProductByIdQuery.class);

  @Test
  void applicationInputsShouldBeImmutable() {

    APPLICATION_INPUT_TYPES.forEach(
        inputType ->
            assertThat(inputType.isRecord() || Modifier.isFinal(inputType.getModifiers()))
                .as("%s must be immutable", inputType.getName())
                .isTrue());
  }

  @Test
  void applicationInputsShouldNotBeDomainAggregates() {

    APPLICATION_INPUT_TYPES.forEach(
        inputType ->
            assertThat(
                    com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct
                        .class
                        .isAssignableFrom(inputType))
                .isFalse());
  }
}
