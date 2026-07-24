package com.shoestore.modules.architecturefixture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.shoestore.shared.domain.model.AggregateRoot;
import com.shoestore.shared.persistence.BaseEntity;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class DomainModelArchitectureTest {

  private final JavaClasses importedClasses =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .importPackages("com.shoestore");

  @Test
  void aggregateRootContractShouldRemainAnInterface() {
    classes()
        .that()
        .haveFullyQualifiedName(AggregateRoot.class.getName())
        .should()
        .beInterfaces()
        .check(importedClasses);
  }

  @Test
  void aggregateRootsShouldReuseBaseEntityIdentity() {
    classes()
        .that()
        .implement(AggregateRoot.class)
        .should()
        .beAssignableTo(BaseEntity.class)
        .allowEmptyShould(true)
        .check(importedClasses);
  }

  @Test
  void domainModelsShouldNotDeclarePublicSetters() {
    methods()
        .that()
        .areDeclaredInClassesThat()
        .resideInAPackage("..domain.model..")
        .and()
        .haveNameMatching("set[A-Z].*")
        .should()
        .notBePublic()
        .allowEmptyShould(true)
        .check(importedClasses);
  }

  @Test
  void domainShouldNotDependOnApplicationLayer() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..application..")
        .check(importedClasses);
  }

  @Test
  void domainShouldNotDependOnPresentationLayer() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..presentation..")
        .check(importedClasses);
  }

  @Test
  void domainShouldNotDependOnInfrastructureLayer() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..infrastructure..")
        .check(importedClasses);
  }

  @Test
  void domainShouldNotDependOnSpringWeb() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.springframework.web..", "org.springframework.http..")
        .check(importedClasses);
  }

  @Test
  void domainShouldNotDependOnPersistenceRuntimeApis() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.hibernate..",
            "org.springframework.data.repository..",
            "org.springframework.data.jpa.repository..",
            "org.springframework.jdbc..")
        .check(importedClasses);
  }

  @Test
  void domainShouldNotDependOnEntityManager() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .haveFullyQualifiedName("jakarta.persistence.EntityManager")
        .check(importedClasses);
  }
}
