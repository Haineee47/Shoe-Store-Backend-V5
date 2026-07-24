package com.shoestore.modules.architecturefixture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ApplicationPortArchitectureTest {

  private static final String PORT_PACKAGE = "com.shoestore.modules..application.port..";

  private static final JavaClasses PROJECT_CLASSES =
      new ClassFileImporter().importPackages("com.shoestore");

  @Test
  void portsShouldResideInApplicationPortPackage() {

    classes()
        .that()
        .haveSimpleNameEndingWith("Port")
        .and()
        .resideInAPackage("com.shoestore.modules..application..")
        .should()
        .resideInAPackage(PORT_PACKAGE)
        .because("Application outbound ports must belong to application.port")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void applicationPortPackageShouldContainInterfacesOnly() {

    classes()
        .that()
        .resideInAPackage(PORT_PACKAGE)
        .should()
        .beInterfaces()
        .because("Application ports define contracts, not implementations")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void applicationPortsShouldNotDependOnInfrastructureOrPresentation() {

    noClasses()
        .that()
        .resideInAPackage(PORT_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..infrastructure..",
            "..presentation..",
            "..controller..",
            "..request..",
            "..response..")
        .because("Application ports must not expose adapter or transport concerns")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void applicationPortsShouldNotDependOnFrameworks() {

    noClasses()
        .that()
        .resideInAPackage(PORT_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework..",
            "jakarta.persistence..",
            "javax.persistence..",
            "org.hibernate..",
            "jakarta.servlet..",
            "javax.servlet..",
            "java.sql..",
            "javax.sql..")
        .because("Application ports must remain framework-independent")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void applicationLayerShouldNotDependOnInfrastructureAdapters() {

    noClasses()
        .that()
        .resideInAPackage("com.shoestore.modules..application..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.shoestore.modules..infrastructure..")
        .because("Application must depend on ports, never concrete infrastructure adapters")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }
}
