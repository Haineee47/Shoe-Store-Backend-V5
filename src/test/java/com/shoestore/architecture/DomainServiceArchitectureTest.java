package com.shoestore.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DomainServiceArchitectureTest {

  private static final String BASE_PACKAGE = "com.shoestore";

  private static final String MODULE_DOMAIN_SERVICES = "..modules..domain.service..";

  private static final String SHARED_DOMAIN_SERVICES = "..shared.domain.service..";

  private static final String[] DOMAIN_SERVICE_PACKAGES = {
    MODULE_DOMAIN_SERVICES, SHARED_DOMAIN_SERVICES
  };

  private static final Set<String> IMMUTABLE_CONSTANT_TYPES =
      Set.of(
          String.class.getName(),
          Boolean.class.getName(),
          Byte.class.getName(),
          Short.class.getName(),
          Integer.class.getName(),
          Long.class.getName(),
          Float.class.getName(),
          Double.class.getName(),
          Character.class.getName());

  private static JavaClasses productionClasses;

  @BeforeAll
  static void importProductionClasses() {
    productionClasses =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);
  }

  @Test
  void domainServicesShouldUseDomainOrientedNames() {
    ArchRule rule =
        classes()
            .that()
            .resideInAnyPackage(DOMAIN_SERVICE_PACKAGES)
            .should()
            .haveSimpleNameEndingWith("Policy")
            .orShould()
            .haveSimpleNameEndingWith("Calculator")
            .orShould()
            .haveSimpleNameEndingWith("Evaluator")
            .orShould()
            .haveSimpleNameEndingWith("Eligibility")
            .orShould()
            .haveSimpleNameEndingWith("Resolver")
            .orShould()
            .haveSimpleNameEndingWith("Allocator")
            .orShould()
            .haveSimpleNameEndingWith("Specification")
            .allowEmptyShould(true)
            .because("Domain Services must use names that express business responsibility");

    rule.check(productionClasses);
  }

  @Test
  void domainServicesShouldNotDependOnSpring() {
    noClasses()
        .that()
        .resideInAnyPackage(DOMAIN_SERVICE_PACKAGES)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.springframework..")
        .allowEmptyShould(true)
        .because("pure Domain Services must remain independent of Spring")
        .check(productionClasses);
  }

  @Test
  void domainServicesShouldNotDependOnPersistence() {
    noClasses()
        .that()
        .resideInAnyPackage(DOMAIN_SERVICE_PACKAGES)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "jakarta.persistence..",
            "org.hibernate..",
            "java.sql..",
            "javax.sql..",
            "org.springframework.data..",
            "org.springframework.jdbc..",
            "..repository..",
            "..persistence..")
        .allowEmptyShould(true)
        .because("persistence access belongs to the application and infrastructure layers")
        .check(productionClasses);
  }

  @Test
  void domainServicesShouldNotDependOnOuterLayers() {
    noClasses()
        .that()
        .resideInAnyPackage(DOMAIN_SERVICE_PACKAGES)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..application..",
            "..presentation..",
            "..controller..",
            "..web..",
            "..api..",
            "..infrastructure..",
            "..adapter..",
            "..client..")
        .allowEmptyShould(true)
        .because("outer layers must depend on domain, never the reverse")
        .check(productionClasses);
  }

  @Test
  void domainServicesShouldNotDependOnTransportOrIntegrationApis() {
    noClasses()
        .that()
        .resideInAnyPackage(DOMAIN_SERVICE_PACKAGES)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "jakarta.servlet..",
            "org.springframework.http..",
            "org.springframework.web..",
            "org.springframework.kafka..",
            "org.springframework.amqp..",
            "org.springframework.jms..",
            "org.springframework.messaging..",
            "org.springframework.cache..",
            "org.springframework.data.redis..")
        .allowEmptyShould(true)
        .because("transport, messaging and caching are not domain concerns")
        .check(productionClasses);
  }

  @Test
  void domainServicesShouldNotDependOnSecurityOrLogging() {
    noClasses()
        .that()
        .resideInAnyPackage(DOMAIN_SERVICE_PACKAGES)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework.security..",
            "org.slf4j..",
            "org.apache.logging..",
            "ch.qos.logback..",
            "..logging..")
        .allowEmptyShould(true)
        .because("security context and logging belong at application boundaries")
        .check(productionClasses);
  }

  @Test
  void domainServicesShouldNotBeSpringComponents() {
    noClasses()
        .that()
        .resideInAnyPackage(DOMAIN_SERVICE_PACKAGES)
        .should()
        .beAnnotatedWith("org.springframework.stereotype.Service")
        .allowEmptyShould(true)
        .check(productionClasses);

    noClasses()
        .that()
        .resideInAnyPackage(DOMAIN_SERVICE_PACKAGES)
        .should()
        .beAnnotatedWith("org.springframework.stereotype.Component")
        .allowEmptyShould(true)
        .check(productionClasses);
  }

  @Test
  void domainServicesShouldNotDeclareTransactionBoundaries() {
    noClasses()
        .that()
        .resideInAnyPackage(DOMAIN_SERVICE_PACKAGES)
        .should()
        .beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
        .allowEmptyShould(true)
        .because("transaction boundaries belong to Application Services")
        .check(productionClasses);
  }

  @Test
  void domainServicesShouldNotDeclareMutableStaticState() {
    fields()
        .that()
        .areDeclaredInClassesThat()
        .resideInAnyPackage(DOMAIN_SERVICE_PACKAGES)
        .and()
        .areStatic()
        .should(beImmutableStaticConstant())
        .allowEmptyShould(true)
        .because("Domain Services must not share mutable runtime state")
        .check(productionClasses);
  }

  private static ArchCondition<JavaField> beImmutableStaticConstant() {
    return new ArchCondition<>("be a static final immutable constant") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        boolean finalField = field.getModifiers().contains(JavaModifier.FINAL);

        boolean immutableType =
            field.getRawType().isPrimitive()
                || field.getRawType().isEnum()
                || IMMUTABLE_CONSTANT_TYPES.contains(field.getRawType().getName());

        boolean valid = finalField && immutableType;

        String message =
            field.getFullName() + " must be final and use a supported immutable constant type";

        events.add(new SimpleConditionEvent(field, valid, message));
      }
    };
  }
}
