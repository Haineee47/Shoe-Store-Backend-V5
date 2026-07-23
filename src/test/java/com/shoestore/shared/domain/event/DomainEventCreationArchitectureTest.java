package com.shoestore.shared.domain.event;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainEventCreationArchitectureTest {
  private static final JavaClasses DOMAIN_EVENT_CLASSES =
      new ClassFileImporter().importPackages("com.shoestore.shared.domain.event.fixture");

  @Test
  void aggregatesAndEventsShouldNotGenerateRandomEventIds() {
    noClasses()
        .that()
        .resideInAPackage("com.shoestore.shared.domain.event.fixture..")
        .should()
        .callMethod(UUID.class, "randomUUID")
        .check(DOMAIN_EVENT_CLASSES);
  }

  @Test
  void aggregatesAndEventsShouldNotReadCurrentSystemTime() {
    noClasses()
        .that()
        .resideInAPackage("com.shoestore.shared.domain.event.fixture..")
        .should()
        .callMethod(Instant.class, "now")
        .check(DOMAIN_EVENT_CLASSES);
  }
}
