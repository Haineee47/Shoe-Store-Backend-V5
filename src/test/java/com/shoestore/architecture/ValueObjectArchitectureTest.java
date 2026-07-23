package com.shoestore.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;

@AnalyzeClasses(packages = "com.shoestore", importOptions = ImportOption.DoNotIncludeTests.class)
class ValueObjectArchitectureTest {

  @ArchTest
  static final ArchRule valueObjectsMustNotBeEntities =
      noClasses()
          .that()
          .resideInAPackage("..valueobject..")
          .should()
          .beAnnotatedWith(Entity.class)
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule valueObjectsMustNotExtendBaseEntity =
      noClasses()
          .that()
          .resideInAPackage("..valueobject..")
          .should()
          .beAssignableTo(com.shoestore.shared.persistence.BaseEntity.class)
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule valueObjectsMustNotDependOnSpring =
      noClasses()
          .that()
          .resideInAPackage("..valueobject..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule valueObjectsMustNotDependOnPersistence =
      noClasses()
          .that()
          .resideInAPackage("..valueobject..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("jakarta.persistence..", "org.hibernate..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule valueObjectsMustNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("..valueobject..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("java.sql..", "javax.sql..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule valueObjectsMustNotExposePublicSetters =
      classes()
          .that()
          .resideInAPackage("..valueobject..")
          .should()
          .haveOnlyFinalFields()
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule valueObjectsMustNotDependOnRepositories =
      noClasses()
          .that()
          .resideInAPackage("..valueobject..")
          .should()
          .dependOnClassesThat()
          .haveSimpleNameEndingWith("Repository")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule valueObjectsMustNotDependOnEntityManager =
      noClasses()
          .that()
          .resideInAPackage("..valueobject..")
          .should()
          .dependOnClassesThat()
          .haveSimpleName("EntityManager")
          .allowEmptyShould(true);
}
