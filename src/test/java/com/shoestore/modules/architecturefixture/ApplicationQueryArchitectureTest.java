package com.shoestore.modules.architecturefixture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ApplicationQueryArchitectureTest {

  private static final String QUERY_PACKAGE = "com.shoestore.modules..application.query..";

  private static final JavaClasses PROJECT_CLASSES =
      new ClassFileImporter().importPackages("com.shoestore");

  @Test
  void queriesShouldResideInQueryPackage() {

    classes()
        .that()
        .haveSimpleNameEndingWith("Query")
        .should()
        .resideInAPackage(QUERY_PACKAGE)
        .because("application Queries must belong to the application query package")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void queryPackageShouldContainOnlyQueries() {

    classes()
        .that()
        .resideInAPackage(QUERY_PACKAGE)
        .should()
        .haveSimpleNameEndingWith("Query")
        .because("the query package must not become a generic application dumping ground")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void queriesShouldNotDependOnForbiddenLayersOrFrameworks() {

    noClasses()
        .that()
        .resideInAPackage(QUERY_PACKAGE)
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
        .because("Queries must remain framework-independent application input models")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }

  @Test
  void queriesShouldNotImplementSerializableByDefault() {

    noClasses()
        .that()
        .resideInAPackage(QUERY_PACKAGE)
        .should()
        .implement("java.io.Serializable")
        .because("Queries are in-process application inputs, not transport messages")
        .allowEmptyShould(true)
        .check(PROJECT_CLASSES);
  }
}
