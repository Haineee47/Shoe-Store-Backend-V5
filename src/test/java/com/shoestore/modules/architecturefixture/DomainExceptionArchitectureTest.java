package com.shoestore.modules.architecturefixture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.shoestore.shared.domain.exception.DomainErrorCode;
import com.shoestore.shared.domain.exception.DomainException;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DomainExceptionArchitectureTest {

  private static JavaClasses productionClasses;

  @BeforeAll
  static void importProductionClasses() {
    productionClasses =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.shoestore");
  }

  @Test
  void sharedDomainExceptionFoundationShouldRemainIndependent() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..shared.domain.exception..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..application..",
                "..web..",
                "..controller..",
                "..infrastructure..",
                "..persistence..",
                "..repository..",
                "..logging..")
            .because(
                "shared domain exception foundations must remain " + "independent of outer layers")
            .allowEmptyShould(true);

    rule.check(productionClasses);
  }

  @Test
  void moduleDomainExceptionPackagesShouldRemainIndependent() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..modules..domain.exception..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..application..",
                "..web..",
                "..controller..",
                "..infrastructure..",
                "..persistence..",
                "..logging..")
            .because(
                "module domain exceptions must contain only "
                    + "domain-level business failure concepts")
            .allowEmptyShould(true);

    rule.check(productionClasses);
  }

  @Test
  void domainErrorCodeImplementationsShouldResideInDomainExceptionPackages() {
    ArchRule rule =
        classes()
            .that()
            .implement(DomainErrorCode.class)
            .should()
            .resideInAPackage("..domain.exception..")
            .because("domain error-code ownership belongs to the " + "domain exception package")
            .allowEmptyShould(true);

    rule.check(productionClasses);
  }

  @Test
  void domainErrorCodeImplementationsShouldBeEnums() {
    ArchRule rule =
        classes()
            .that()
            .implement(DomainErrorCode.class)
            .should()
            .beEnums()
            .because("module domain errors use closed and explicit " + "enum identities")
            .allowEmptyShould(true);

    rule.check(productionClasses);
  }

  @Test
  void domainErrorCodesShouldNotDependOnFrameworks() {
    ArchRule rule =
        noClasses()
            .that()
            .implement(DomainErrorCode.class)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "jakarta.servlet..",
                "com.fasterxml.jackson..",
                "..application..",
                "..web..",
                "..controller..",
                "..infrastructure..",
                "..logging..")
            .because(
                "domain error identities must not contain "
                    + "framework, HTTP, persistence, logging, "
                    + "or serialization concerns")
            .allowEmptyShould(true);

    rule.check(productionClasses);
  }

  @Test
  void controllersShouldNotDependOnDomainExceptions() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAnyPackage("..controller..", "..web..controller..")
            .should()
            .dependOnClassesThat()
            .areAssignableTo(DomainException.class)
            .because("controllers must consume application-level " + "failure contracts")
            .allowEmptyShould(true);

    rule.check(productionClasses);
  }

  @Test
  void sharedExceptionHandlingShouldNotDependOnModuleDomainErrorCodes() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAnyPackage("..shared.web..", "..shared.exception..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..modules..domain.exception..")
            .because(
                "shared exception handling must not own mappings "
                    + "for every module domain error")
            .allowEmptyShould(true);

    rule.check(productionClasses);
  }

  @Test
  void domainExceptionTranslatorsShouldResideInApplicationLayer() {
    ArchRule rule =
        classes()
            .that()
            .haveSimpleNameEndingWith("DomainExceptionTranslator")
            .should()
            .resideInAPackage("..application.exception..")
            .because(
                "domain-to-application translation belongs to " + "the module application boundary")
            .allowEmptyShould(true);

    rule.check(productionClasses);
  }

  @Test
  void domainExceptionTranslatorsShouldRemainInfrastructureFree() {
    ArchRule rule =
        noClasses()
            .that()
            .haveSimpleNameEndingWith("DomainExceptionTranslator")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..web..",
                "..controller..",
                "..infrastructure..",
                "..persistence..",
                "..repository..",
                "..logging..",
                "org.springframework.http..",
                "jakarta.persistence..",
                "jakarta.servlet..")
            .because("domain exception translators perform explicit " + "application mapping only")
            .allowEmptyShould(true);

    rule.check(productionClasses);
  }

  @Test
  void domainExceptionsShouldOnlyBeConstructedInsideDomainLayer() {
    ArchRule rule =
        noClasses()
            .that()
            .resideOutsideOfPackage("..domain..")
            .should(constructDomainExceptions())
            .because(
                "domain exceptions represent business-rule "
                    + "violations detected by domain objects "
                    + "or domain services")
            .allowEmptyShould(true);

    rule.check(productionClasses);
  }

  private static ArchCondition<JavaClass> constructDomainExceptions() {
    return new ArchCondition<>("construct DomainException implementations") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        for (JavaConstructorCall call : javaClass.getConstructorCallsFromSelf()) {

          if (!call.getTargetOwner().isAssignableTo(DomainException.class)) {
            continue;
          }

          String message =
              javaClass.getName()
                  + " constructs "
                  + call.getTargetOwner().getName()
                  + " at "
                  + call.getSourceCodeLocation();

          events.add(SimpleConditionEvent.violated(javaClass, message));
        }
      }
    };
  }
}
