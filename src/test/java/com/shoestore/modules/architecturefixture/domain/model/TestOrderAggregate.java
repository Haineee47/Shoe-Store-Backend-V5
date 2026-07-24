package com.shoestore.modules.architecturefixture.domain.model;

import com.shoestore.modules.architecturefixture.domain.model.internal.TestOrderLine;
import com.shoestore.modules.architecturefixture.domain.valueobject.TestOrderId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TestOrderAggregate {

  private final TestOrderId id;
  private final List<TestOrderLine> lines;
  private boolean completed;

  public TestOrderAggregate(TestOrderId id, List<TestOrderLine> lines) {

    this.id = Objects.requireNonNull(id, "id must not be null");

    Objects.requireNonNull(lines, "lines must not be null");

    if (lines.isEmpty()) {
      throw new IllegalArgumentException("order must contain at least one line");
    }

    this.lines = new ArrayList<>(lines);
  }

  public TestOrderId id() {
    return id;
  }

  public boolean isCompleted() {
    return completed;
  }

  public int lineCount() {
    return lines.size();
  }

  public boolean areAllLinesFulfilled() {
    return lines.stream().allMatch(TestOrderLine::isFulfilled);
  }

  public void complete() {

    if (completed) {
      throw new IllegalStateException("order is already completed");
    }

    lines.forEach(TestOrderLine::fulfill);

    completed = true;
  }
}
