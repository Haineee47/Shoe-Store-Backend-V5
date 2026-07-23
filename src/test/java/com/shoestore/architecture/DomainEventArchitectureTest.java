package com.shoestore.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.shoestore.shared.domain.event.DomainEvent;
import com.shoestore.shared.domain.event.DomainEventMetadata;
import com.shoestore.shared.domain.event.DomainEventRegistry;
import com.shoestore.shared.domain.model.AggregateRoot;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainEventArchitectureTest {

  private static final String SHARED_DOMAIN_PACKAGE = "com.shoestore.shared.domain..";

  private static final String DOMAIN_EVENT_PACKAGE = "com.shoestore.shared.domain.event..";

  private static final String DOMAIN_EVENT_FIXTURE_PACKAGE =
      "com.shoestore.shared.domain.event.fixture..";

  private static final JavaClasses DOMAIN_CLASSES =
      new ClassFileImporter().importPackages("com.shoestore.shared.domain");

  @Test
  void domainEventContractShouldRemainAnInterface() {
    ArchRule rule =
        classes()
            .that()
            .haveFullyQualifiedName(DomainEvent.class.getName())
            .should()
            .beInterfaces()
            .because("DomainEvent is a minimal domain contract");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void concreteDomainEventsShouldImplementDomainEvent() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage(DOMAIN_EVENT_FIXTURE_PACKAGE)
            .and()
            .haveSimpleNameEndingWith("DomainEvent")
            .should()
            .implement(DomainEvent.class)
            .because(
                "every concrete domain event must implement " + "the shared DomainEvent contract");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void concreteDomainEventsShouldBeRecords() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage(DOMAIN_EVENT_FIXTURE_PACKAGE)
            .and()
            .haveSimpleNameEndingWith("DomainEvent")
            .should(beRecords())
            .because("domain events must be immutable fact snapshots");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void domainEventMetadataShouldBeARecord() {
    ArchRule rule =
        classes()
            .that()
            .haveFullyQualifiedName(DomainEventMetadata.class.getName())
            .should(beRecords())
            .because("domain-event metadata must be immutable");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void domainEventImplementationsShouldStayInDomainEventPackages() {
    ArchRule rule =
        classes()
            .that()
            .implement(DomainEvent.class)
            .should()
            .resideInAPackage(DOMAIN_EVENT_PACKAGE)
            .because("domain events belong to the domain-event boundary");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void domainEventLayerShouldNotDependOnSpring() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(DOMAIN_EVENT_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..")
            .because("domain events must remain framework independent");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void domainEventLayerShouldNotDependOnPersistenceApis() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(DOMAIN_EVENT_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("jakarta.persistence..", "org.hibernate..")
            .because("domain events are not persistence models");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void domainEventLayerShouldNotDependOnApplicationLayer() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(DOMAIN_EVENT_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("com.shoestore..application..")
            .because("the domain layer must not depend on application code");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void domainEventLayerShouldNotDependOnInfrastructureLayer() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(DOMAIN_EVENT_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("com.shoestore..infrastructure..", "com.shoestore..persistence..")
            .because("domain events must remain infrastructure independent");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void domainLayerShouldNotUseSpringEventPublisher() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(SHARED_DOMAIN_PACKAGE)
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("org.springframework.context.ApplicationEventPublisher")
            .because("domain objects must register events rather than " + "publish them directly");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void domainLayerShouldNotUseSpringApplicationEvents() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage(SHARED_DOMAIN_PACKAGE)
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("org.springframework.context.ApplicationEvent")
            .because("DomainEvent is independent from Spring events");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void aggregatesAndEventsShouldNotGenerateRandomEventIds() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAnyPackage(DOMAIN_EVENT_PACKAGE)
            .should()
            .callMethod(UUID.class, "randomUUID")
            .because("event IDs must be supplied by the application boundary");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void aggregatesAndEventsShouldNotReadCurrentSystemTime() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAnyPackage(DOMAIN_EVENT_PACKAGE)
            .should()
            .callMethod(Instant.class, "now")
            .because("event occurrence time must be supplied explicitly");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void domainEventRegistryFieldsShouldNotBeStatic() {
    ArchRule rule =
        fields()
            .that()
            .haveRawType(DomainEventRegistry.class)
            .should(notBeStatic())
            .because("pending events must be isolated per aggregate instance");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void domainEventRegistryShouldNotDependOnFrameworks() {
    ArchRule rule =
        noClasses()
            .that()
            .haveFullyQualifiedName(DomainEventRegistry.class.getName())
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.hibernate..")
            .because("DomainEventRegistry is a plain domain utility");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void onlyAggregateRootsShouldOwnDomainEventRegistry() {
    ArchRule rule =
        fields()
            .that()
            .haveRawType(DomainEventRegistry.class)
            .should(beDeclaredInAggregateRoots())
            .because("pending domain events belong to aggregate roots");

    rule.check(DOMAIN_CLASSES);
  }

  private static ArchCondition<JavaClass> beRecords() {
    return new ArchCondition<>("be records") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        boolean record = javaClass.reflect().isRecord();

        String message = javaClass.getName() + (record ? " is a record" : " is not a record");

        events.add(new SimpleConditionEvent(javaClass, record, message));
      }
    };
  }

  private static ArchCondition<JavaField> notBeStatic() {
    return new ArchCondition<>("not be static") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        boolean nonStatic =
            !field.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.STATIC);

        String message = field.getFullName() + (nonStatic ? " is not static" : " is static");

        events.add(new SimpleConditionEvent(field, nonStatic, message));
      }
    };
  }

  private static ArchCondition<JavaField> beDeclaredInAggregateRoots() {

    return new ArchCondition<>("be declared in aggregate roots") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        JavaClass owner = field.getOwner();

        boolean aggregateRoot = owner.isAssignableTo(AggregateRoot.class);

        String message =
            field.getFullName()
                + (aggregateRoot
                    ? " is owned by an AggregateRoot"
                    : " is not owned by an AggregateRoot");

        events.add(new SimpleConditionEvent(field, aggregateRoot, message));
      }
    };
  }

  @Test
  void aggregateRootsShouldNotGenerateRandomEventIds() {
    ArchRule rule =
        noClasses()
            .that()
            .implement(AggregateRoot.class)
            .should()
            .callMethod(UUID.class, "randomUUID")
            .because("aggregate roots must receive deterministic " + "domain-event IDs");

    rule.check(DOMAIN_CLASSES);
  }

  @Test
  void aggregateRootsShouldNotReadCurrentSystemTime() {
    ArchRule rule =
        noClasses()
            .that()
            .implement(AggregateRoot.class)
            .should()
            .callMethod(Instant.class, "now")
            .because(
                "aggregate roots must receive deterministic " + "domain-event occurrence times");

    rule.check(DOMAIN_CLASSES);
  }
}
