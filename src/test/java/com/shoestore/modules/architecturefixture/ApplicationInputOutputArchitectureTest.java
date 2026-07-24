package com.shoestore.modules.architecturefixture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ApplicationInputOutputArchitectureTest {

  private static final String APPLICATION_PACKAGE = "com.shoestore.modules..application..";

  private static final String APPLICATION_DTO_PACKAGE = "com.shoestore.modules..application.dto..";

  private static final JavaClasses PROJECT_CLASSES =
      new ClassFileImporter().importPackages("com.shoestore");

  @Test
  void applicationLayerShouldNotDependOnPresentationModels() {

    noClasses()
        .that()
        .resideInAPackage(APPLICATION_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..presentation..", "..controller..", "..request..", "..response..")
        .because("Application inputs and outputs must remain independent from presentation models")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void applicationLayerShouldNotDependOnPersistenceEntities() {

    noClasses()
        .that()
        .resideInAPackage(APPLICATION_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..infrastructure.persistence.entity..", "..persistence.entity..")
        .because("Application use cases must not expose or consume persistence entities")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void applicationDtosShouldNotDependOnInfrastructureOrPresentation() {

    noClasses()
        .that()
        .resideInAPackage(APPLICATION_DTO_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..infrastructure..",
            "..presentation..",
            "..controller..",
            "..request..",
            "..response..")
        .because("Application outputs must remain owned by the application layer")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void applicationDtosShouldNotDependOnJpaOrSpringWeb() {

    noClasses()
        .that()
        .resideInAPackage(APPLICATION_DTO_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "jakarta.persistence..",
            "javax.persistence..",
            "org.hibernate..",
            "org.springframework.web..",
            "jakarta.servlet..",
            "javax.servlet..")
        .because("Application outputs must not carry persistence or HTTP concerns")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }
}
