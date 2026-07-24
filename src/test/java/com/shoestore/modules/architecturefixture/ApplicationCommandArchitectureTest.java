package com.shoestore.modules.architecturefixture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ApplicationCommandArchitectureTest {

  private static final String COMMAND_PACKAGE = "com.shoestore.modules..application.command..";

  private static final JavaClasses PROJECT_CLASSES =
      new ClassFileImporter().importPackages("com.shoestore");

  @Test
  void commandsShouldResideInCommandPackage() {

    classes()
        .that()
        .haveSimpleNameEndingWith("Command")
        .should()
        .resideInAPackage(COMMAND_PACKAGE)
        .because("application Commands must be owned by the application command package")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void commandPackageShouldContainOnlyCommands() {

    classes()
        .that()
        .resideInAPackage(COMMAND_PACKAGE)
        .should()
        .haveSimpleNameEndingWith("Command")
        .because("the command package must not become a generic application dumping ground")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void commandsShouldNotDependOnForbiddenLayersOrFrameworks() {

    noClasses()
        .that()
        .resideInAPackage(COMMAND_PACKAGE)
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
            "javax.sql..",
            "..infrastructure..",
            "..presentation..",
            "..controller..",
            "..request..",
            "..response..")
        .because("Commands must remain framework-independent application input models")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void commandsShouldNotImplementFrameworkInterfaces() {

    noClasses()
        .that()
        .resideInAPackage(COMMAND_PACKAGE)
        .should()
        .implement("java.io.Serializable")
        .because("Commands must not inherit serialization concerns by default")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }
}
