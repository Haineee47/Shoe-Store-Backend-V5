package com.shoestore.modules.architecturefixture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ApplicationServiceArchitectureTest {

  private static final String APPLICATION_SERVICE_PACKAGE =
      "com.shoestore.modules..application.service..";

  private static final JavaClasses PROJECT_CLASSES =
      new ClassFileImporter().importPackages("com.shoestore");

  @Test
  void useCasesShouldResideInApplicationServicePackage() {

    classes()
        .that()
        .haveSimpleNameEndingWith("UseCase")
        .should()
        .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
        .because("Use-case implementations belong to the application service package")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void useCasesShouldBeFinal() {

    classes()
        .that()
        .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
        .and()
        .haveSimpleNameEndingWith("UseCase")
        .should()
        .haveModifier(JavaModifier.FINAL)
        .because("Application use cases are concrete orchestration boundaries")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void applicationServicesShouldNotDependOnInfrastructureOrPresentation() {

    noClasses()
        .that()
        .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..infrastructure..",
            "..presentation..",
            "..controller..",
            "..request..",
            "..response..")
        .because(
            "Application services depend on domain contracts and application ports, not adapters")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void applicationServicesShouldNotDependOnPersistenceFrameworks() {

    noClasses()
        .that()
        .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "jakarta.persistence..",
            "javax.persistence..",
            "org.hibernate..",
            "org.springframework.data..",
            "java.sql..",
            "javax.sql..")
        .because("Application services must not perform persistence implementation work")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void applicationServicesShouldNotDependOnHttpFrameworks() {

    noClasses()
        .that()
        .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.springframework.web..", "jakarta.servlet..", "javax.servlet..")
        .because("HTTP concerns belong to the presentation layer")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void applicationServicesShouldNotDependOnAggregateInternalChildren() {

    noClasses()
        .that()
        .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..domain.model.internal..")
        .because(
            "Application services must interact with Aggregate Roots and must not mutate child entities directly")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void domainModelShouldNotDependOnApplicationServices() {

    noClasses()
        .that()
        .resideInAPackage("com.shoestore.modules..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
        .because("Domain must remain independent from application orchestration")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }
}
