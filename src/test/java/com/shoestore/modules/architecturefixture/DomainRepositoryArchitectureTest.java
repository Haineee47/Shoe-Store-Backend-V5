package com.shoestore.modules.architecturefixture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

class DomainRepositoryArchitectureTest {

  private static final String MODULE_ROOT_PACKAGE =
      "com.shoestore.modules";

  private static final String DOMAIN_REPOSITORY_PACKAGE =
      "com.shoestore.modules..domain.repository..";

  private static final String DOMAIN_REPOSITORY_SUPPORT_PACKAGE =
      "com.shoestore.modules..domain.repository.support..";

  private static JavaClasses importedClasses;

  @BeforeAll
  static void importProjectClasses() {
    importedClasses =
        new ClassFileImporter()
            .importPackages(
                "com.shoestore.modules",
                "com.shoestore.shared");
  }

  @Test
  void domainRepositoriesShouldBeInterfaces() {
    classes()
        .that()
        .resideInAPackage(
            DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(
            DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith(
            "Repository")
        .should()
        .beInterfaces()
        .because(
            "Domain Repositories are domain-owned ports, not concrete implementations")
        .check(importedClasses);
  }

  @Test
  void domainRepositoriesShouldNotDependOnSpring() {
    noClasses()
        .that()
        .resideInAPackage(
            DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(
            DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith(
            "Repository")
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
        .resideInAPackage(
            DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(
            DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith(
            "Repository")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "jakarta.persistence..",
            "javax.persistence..",
            "org.hibernate..")
        .because(
            "persistence frameworks belong to infrastructure adapters")
        .check(importedClasses);
  }

  @Test
  void domainRepositoriesShouldNotDependOnSqlApis() {
    noClasses()
        .that()
        .resideInAPackage(
            DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(
            DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith(
            "Repository")
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
        .resideInAPackage(
            DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(
            DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith(
            "Repository")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "com.shoestore.shared.persistence..")
        .because(
            "shared persistence abstractions are infrastructure concerns")
        .check(importedClasses);
  }

  @Test
  void domainRepositoriesShouldNotDependOnOuterLayers() {
    noClasses()
        .that()
        .resideInAPackage(
            DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(
            DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith(
            "Repository")
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
        .resideInAPackage(
            DOMAIN_REPOSITORY_PACKAGE)
        .and()
        .resideOutsideOfPackage(
            DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith(
            "Repository")
        .should()
        .notBeAnnotatedWith(
            Repository.class)
        .andShould()
        .notBeAnnotatedWith(
            Component.class)
        .andShould()
        .notBeAnnotatedWith(
            Transactional.class)
        .andShould()
        .notBeAnnotatedWith(
            NoRepositoryBean.class)
        .because(
            "pure Domain Repository ports must not carry framework annotations")
        .check(importedClasses);
  }

  @Test
  void domainRepositoryContractsShouldBelongToTheirOwnModule() {
    Set<String> violations =
        importedClasses.stream()
            .filter(
                this::isDomainRepositoryContract)
            .flatMap(
                repository ->
                    repository
                        .getDirectDependenciesFromSelf()
                        .stream()
                        .filter(
                            dependency ->
                                violatesModuleOwnership(
                                    repository,
                                    dependency))
                        .map(
                            dependency ->
                                formatOwnershipViolation(
                                    repository,
                                    dependency)))
            .collect(
                Collectors.toCollection(
                    java.util.LinkedHashSet::new));

    assertThat(violations)
        .as(
            "Domain Repository contracts must not depend on domain types owned by another business module")
        .isEmpty();
  }

  @Test
  void repositorySupportImplementationsShouldRemainSeparated() {
    classes()
        .that()
        .resideInAPackage(
            DOMAIN_REPOSITORY_SUPPORT_PACKAGE)
        .and()
        .haveSimpleNameEndingWith(
            "Repository")
        .should()
        .notBeInterfaces()
        .because(
            "repository.support is reserved for test or fixture implementations rather than domain contracts")
        .check(importedClasses);
  }

  private boolean isDomainRepositoryContract(
      JavaClass javaClass) {

    String packageName =
        javaClass.getPackageName();

    return packageName.startsWith(
            MODULE_ROOT_PACKAGE + ".")
        && packageName.contains(
            ".domain.repository")
        && !packageName.contains(
            ".domain.repository.support")
        && javaClass
            .getSimpleName()
            .endsWith("Repository");
  }

  private boolean violatesModuleOwnership(
      JavaClass repository,
      Dependency dependency) {

    JavaClass targetClass =
        dependency.getTargetClass();

    String targetPackage =
        targetClass.getPackageName();

    if (!targetPackage.startsWith(
        MODULE_ROOT_PACKAGE + ".")) {

      return false;
    }

    String repositoryModule =
        extractModuleName(
            repository.getPackageName());

    String targetModule =
        extractModuleName(
            targetPackage);

    if (repositoryModule == null
        || targetModule == null) {

      return false;
    }

    if (repositoryModule.equals(
        targetModule)) {

      return false;
    }

    return targetPackage.contains(
        ".domain.");
  }

  private String extractModuleName(
      String packageName) {

    String prefix =
        MODULE_ROOT_PACKAGE + ".";

    if (!packageName.startsWith(prefix)) {
      return null;
    }

    String remaining =
        packageName.substring(
            prefix.length());

    int separatorIndex =
        remaining.indexOf('.');

    if (separatorIndex < 0) {
      return remaining;
    }

    return remaining.substring(
        0,
        separatorIndex);
  }

  private String formatOwnershipViolation(
      JavaClass repository,
      Dependency dependency) {

    return repository.getName()
        + " depends on cross-module domain type "
        + dependency
            .getTargetClass()
            .getName();
  }
}
