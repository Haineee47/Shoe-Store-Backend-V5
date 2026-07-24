package com.shoestore.modules.architecturefixture.domain.repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

class DomainRepositoryFrameworkIsolationTest {

  private static final String DOMAIN_REPOSITORY_PACKAGE =
      "com.shoestore.modules.architecturefixture.domain.repository..";

  private static final String DOMAIN_REPOSITORY_SUPPORT_PACKAGE =
      "com.shoestore.modules.architecturefixture.domain.repository.support..";

  private static JavaClasses importedClasses;

  @BeforeAll
  static void importClasses() {
    importedClasses =
        new ClassFileImporter()
            .importPackages(
                "com.shoestore.modules.architecturefixture",
                "com.shoestore.shared");
  }

  @Test
  void domainRepositoriesShouldBeInterfaces() {
    classes()
        .that()
        .resideInAPackage(DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith("Repository")
        .should()
        .beInterfaces()
        .because(
            "Domain Repositories are domain-owned ports, not implementations")
        .check(importedClasses);
  }

  @Test
  void domainRepositoriesShouldNotDependOnSpring() {
    noClasses()
        .that()
        .resideInAPackage(DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith("Repository")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework..",
            "org.springframework.data..")
        .because(
            "Domain Repository contracts must remain independent from Spring and Spring Data")
        .check(importedClasses);
  }

  @Test
  void domainRepositoriesShouldNotDependOnPersistenceFrameworks() {
    noClasses()
        .that()
        .resideInAPackage(DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith("Repository")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "jakarta.persistence..",
            "javax.persistence..",
            "org.hibernate..")
        .because(
            "JPA and Hibernate belong to persistence adapters, not Domain Repository ports")
        .check(importedClasses);
  }

  @Test
  void domainRepositoriesShouldNotDependOnSqlApis() {
    noClasses()
        .that()
        .resideInAPackage(DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith("Repository")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "java.sql..",
            "javax.sql..")
        .because(
            "SQL APIs must not leak into Domain Repository contracts")
        .check(importedClasses);
  }

  @Test
  void domainRepositoriesShouldNotDependOnSharedPersistence() {
    noClasses()
        .that()
        .resideInAPackage(DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith("Repository")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "com.shoestore.shared.persistence..")
        .because(
            "shared.persistence must not be referenced by Domain Repository ports")
        .check(importedClasses);
  }

  @Test
  void domainRepositoriesShouldNotDependOnOuterLayers() {
    noClasses()
        .that()
        .resideInAPackage(DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith("Repository")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "..application..",
            "..infrastructure..",
            "..web..",
            "..controller..",
            "..dto..")
        .because(
            "Domain Repository dependencies must point inward toward the domain")
        .check(importedClasses);
  }

  @Test
  void domainRepositoriesShouldNotCarryFrameworkAnnotations() {
    classes()
        .that()
        .resideInAPackage(DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith("Repository")
        .should()
        .notBeAnnotatedWith(Repository.class)
        .andShould()
        .notBeAnnotatedWith(Component.class)
        .andShould()
        .notBeAnnotatedWith(Transactional.class)
        .andShould()
        .notBeAnnotatedWith(NoRepositoryBean.class)
        .because(
            "pure Domain Repository ports must not carry framework annotations")
        .check(importedClasses);
  }
}
