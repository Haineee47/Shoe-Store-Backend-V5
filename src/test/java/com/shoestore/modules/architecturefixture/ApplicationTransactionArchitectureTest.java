package com.shoestore.modules.architecturefixture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ApplicationTransactionArchitectureTest {

  private static final JavaClasses PROJECT_CLASSES =
      new ClassFileImporter().importPackages("com.shoestore");

  @Test
  void domainShouldNotDependOnTransactionFrameworks() {

    noClasses()
        .that()
        .resideInAPackage("com.shoestore.modules..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework.transaction..", "jakarta.transaction..", "javax.transaction..")
        .because("The Domain Layer must remain unaware of transaction management")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void domainRepositoriesShouldNotDependOnTransactionFrameworks() {

    noClasses()
        .that()
        .resideInAPackage("com.shoestore.modules..domain.repository..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework.transaction..", "jakarta.transaction..", "javax.transaction..")
        .because("Domain Repository contracts must not own transaction boundaries")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void controllersShouldNotDependOnTransactionFrameworks() {

    noClasses()
        .that()
        .resideInAnyPackage("..controller..", "..presentation..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework.transaction..", "jakarta.transaction..", "javax.transaction..")
        .because("Controllers must not own business transactions")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void transactionContractsShouldNotDependOnInfrastructure() {

    noClasses()
        .that()
        .resideInAPackage("com.shoestore.modules..application.transaction..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..infrastructure..",
            "org.springframework.transaction..",
            "jakarta.persistence..",
            "javax.persistence..",
            "org.hibernate..")
        .because(
            "Application transaction semantics must remain independent from implementation mechanisms")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void domainEventsShouldNotDependOnTransactionFrameworks() {

    noClasses()
        .that()
        .resideInAPackage("com.shoestore.modules..domain.event..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework.transaction..",
            "org.springframework.context..",
            "jakarta.transaction..",
            "javax.transaction..")
        .because("Domain Events describe domain facts and must not know transaction timing")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }
}
