package com.shoestore.modules.architecturefixture.application.query;

import com.shoestore.modules.architecturefixture.domain.valueobject.TestApplicationProductId;
import java.util.Objects;

public record FindTestProductByIdQuery(TestApplicationProductId productId) {

  public FindTestProductByIdQuery {
    Objects.requireNonNull(productId, "productId must not be null");
  }
}
