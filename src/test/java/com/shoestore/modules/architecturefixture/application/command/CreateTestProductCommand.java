package com.shoestore.modules.architecturefixture.application.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record CreateTestProductCommand(String name, BigDecimal price, List<String> tags) {

  public CreateTestProductCommand {

    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(price, "price must not be null");
    Objects.requireNonNull(tags, "tags must not be null");

    tags = List.copyOf(tags);
  }
}
