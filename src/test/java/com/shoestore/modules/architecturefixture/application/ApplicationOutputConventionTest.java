package com.shoestore.modules.architecturefixture.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.modules.architecturefixture.application.dto.ActivateTestProductResult;
import com.shoestore.modules.architecturefixture.application.dto.TestProductDetails;
import com.shoestore.modules.architecturefixture.domain.model.TestApplicationProduct;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationOutputConventionTest {

  private static final List<Class<?>> APPLICATION_OUTPUT_TYPES =
      List.of(ActivateTestProductResult.class, TestProductDetails.class);

  @Test
  void applicationOutputsShouldBeImmutable() {

    APPLICATION_OUTPUT_TYPES.forEach(
        outputType ->
            assertThat(outputType.isRecord() || Modifier.isFinal(outputType.getModifiers()))
                .as("%s must be immutable", outputType.getName())
                .isTrue());
  }

  @Test
  void applicationOutputsShouldNotBeDomainAggregates() {

    APPLICATION_OUTPUT_TYPES.forEach(
        outputType ->
            assertThat(TestApplicationProduct.class.isAssignableFrom(outputType))
                .as("%s must not be a Domain Aggregate", outputType.getName())
                .isFalse());
  }

  @Test
  void applicationOutputsShouldBelongToApplicationLayer() {

    APPLICATION_OUTPUT_TYPES.forEach(
        outputType ->
            assertThat(outputType.getPackageName())
                .as("%s must be owned by the application layer", outputType.getName())
                .contains(".application."));
  }

  @Test
  void applicationOutputsShouldNotBelongToForbiddenLayers() {

    APPLICATION_OUTPUT_TYPES.forEach(
        outputType -> {
          String packageName = outputType.getPackageName();

          assertThat(packageName)
              .doesNotContain(".infrastructure.")
              .doesNotContain(".persistence.")
              .doesNotContain(".presentation.")
              .doesNotContain(".controller.")
              .doesNotContain(".request.")
              .doesNotContain(".response.");
        });
  }
}
