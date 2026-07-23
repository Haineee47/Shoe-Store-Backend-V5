package com.shoestore.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.shoestore.shared.domain.specification.Specification;
import com.shoestore.shared.domain.specification.fixture.TestProductHasAvailableStockSpecification;
import com.shoestore.shared.domain.specification.fixture.TestProductIsActiveSpecification;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainSpecificationArchitectureTest {

  private static final String SHARED_SPECIFICATION_PACKAGE =
      "com.shoestore.shared.domain.specification..";

  private static final List<String> FORBIDDEN_DEPENDENCY_PACKAGES =
      List.of(
          "org.springframework..",
          "jakarta.persistence..",
          "javax.persistence..",
          "org.hibernate..",
          "jakarta.servlet..",
          "javax.servlet..",
          "com.shoestore.shared.persistence..",
          "com.shoestore.shared.application..",
          "com.shoestore.shared.infrastructure..",
          "com.shoestore.shared.web..");

  private static final JavaClasses PRODUCTION_CLASSES =
      new ClassFileImporter().importPackages("com.shoestore");

  private static final JavaClasses FIXTURE_CLASSES =
      new ClassFileImporter()
          .importClasses(
              TestProductIsActiveSpecification.class,
              TestProductHasAvailableStockSpecification.class);

  @Test
  void sharedSpecificationPackageShouldNotUseForbiddenDependencies() {
    noClasses()
        .that()
        .resideInAPackage(SHARED_SPECIFICATION_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(FORBIDDEN_DEPENDENCY_PACKAGES.toArray(String[]::new))
        .because("Domain Specifications must remain framework-independent and domain-pure")
        .check(PRODUCTION_CLASSES);
  }

  @Test
  void sharedSpecificationPackageShouldNotDependOnBusinessModules() {
    noClasses()
        .that()
        .resideInAPackage(SHARED_SPECIFICATION_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.shoestore.modules..")
        .because("the shared Specification contract must not depend on a business module")
        .check(PRODUCTION_CLASSES);
  }

  @Test
  void specificationContractShouldRemainAnInterface() {
    classes()
        .that()
        .haveFullyQualifiedName(Specification.class.getName())
        .should()
        .beInterfaces()
        .because("Specification is the shared functional Domain contract")
        .check(PRODUCTION_CLASSES);
  }

  @Test
  void productionSpecificationImplementationsShouldNotUseSpringStereotypes() {
    classes()
        .that()
        .implement(Specification.class)
        .should(haveNoSpringStereotypeAnnotations())
        .because("Specification construction must remain explicit")
        .allowEmptyShould(true)
        .check(PRODUCTION_CLASSES);
  }

  @Test
  void fixtureSpecificationsShouldNotUseForbiddenDependencies() {
    noClasses()
        .that()
        .implement(Specification.class)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(FORBIDDEN_DEPENDENCY_PACKAGES.toArray(String[]::new))
        .because("fixtures must demonstrate the approved dependency boundary")
        .check(FIXTURE_CLASSES);
  }

  @Test
  void fixtureSpecificationsShouldBeFinal() {
    classes()
        .that()
        .implement(Specification.class)
        .should(beFinalClasses())
        .because("named immutable Specifications should not expose inheritance extension points")
        .check(FIXTURE_CLASSES);
  }

  @Test
  void fixtureSpecificationsShouldHaveNoMutableFields() {
    classes()
        .that()
        .implement(Specification.class)
        .should(haveNoMutableFields())
        .because("Specifications must be stateless or immutable")
        .check(FIXTURE_CLASSES);
  }

  private static ArchCondition<JavaClass> haveNoSpringStereotypeAnnotations() {

    List<String> forbiddenAnnotations =
        List.of(
            "org.springframework.stereotype.Component",
            "org.springframework.stereotype.Service",
            "org.springframework.stereotype.Repository",
            "org.springframework.context.annotation.Configuration");

    return new ArchCondition<>("declare no Spring stereotype annotations") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {

        List<String> presentForbiddenAnnotations =
            javaClass.getAnnotations().stream()
                .map(annotation -> annotation.getRawType().getName())
                .filter(forbiddenAnnotations::contains)
                .toList();

        boolean satisfied = presentForbiddenAnnotations.isEmpty();

        String message =
            javaClass.getName()
                + " declares forbidden Spring annotations "
                + presentForbiddenAnnotations;

        events.add(new SimpleConditionEvent(javaClass, satisfied, message));
      }
    };
  }

  private static ArchCondition<JavaClass> beFinalClasses() {

    return new ArchCondition<>("be final") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {

        boolean satisfied = javaClass.getModifiers().contains(JavaModifier.FINAL);

        String message = javaClass.getName() + " must be final";

        events.add(new SimpleConditionEvent(javaClass, satisfied, message));
      }
    };
  }

  private static ArchCondition<JavaClass> haveNoMutableFields() {

    return new ArchCondition<>("have no mutable fields") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {

        javaClass.getFields().stream()
            .filter(field -> field.getOwner().equals(javaClass))
            .forEach(
                field -> {
                  boolean satisfied = field.getModifiers().contains(JavaModifier.FINAL);

                  String message = javaClass.getName() + "." + field.getName() + " must be final";

                  events.add(new SimpleConditionEvent(field, satisfied, message));
                });
      }
    };
  }
}
