package com.shoestore.modules.architecturefixture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.shared.domain.model.AggregateRoot;
import com.shoestore.shared.persistence.BaseEntity;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DomainPolicyArchitectureTest {

  private static final String FIXTURE_POLICY_PACKAGE =
      "com.shoestore.shared.domain.policy.fixture..";

  private static final String[] PRODUCTION_POLICY_PACKAGES = {
    "com.shoestore.shared.domain.policy..", "com.shoestore.modules..domain.policy.."
  };

  private static final String[] FORBIDDEN_DEPENDENCY_PACKAGES = {
    "org.springframework..",
    "jakarta.persistence..",
    "org.hibernate..",
    "java.sql..",
    "javax.sql..",
    "com.shoestore..application..",
    "com.shoestore..infrastructure..",
    "com.shoestore..presentation..",
    "com.shoestore..controller..",
    "com.shoestore..web..",
    "com.shoestore..persistence.."
  };

  private final JavaClasses fixtureClasses =
      new ClassFileImporter().importPackages("com.shoestore.shared.domain.policy.fixture");

  private final JavaClasses productionClasses =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .importPackages("com.shoestore");

  @Test
  void fixturePolicyContractsShouldUsePolicySuffix() {
    classes()
        .that()
        .resideInAnyPackage(FIXTURE_POLICY_PACKAGE)
        .and()
        .areInterfaces()
        .should()
        .haveSimpleNameEndingWith("Policy")
        .check(fixtureClasses);
  }

  @Test
  void fixturePolicyImplementationsShouldBeFinal() {
    classes()
        .that()
        .resideInAnyPackage(FIXTURE_POLICY_PACKAGE)
        .and()
        .areNotInterfaces()
        .and()
        .haveSimpleNameEndingWith("Policy")
        .should()
        .haveModifier(JavaModifier.FINAL)
        .check(fixtureClasses);
  }

  @Test
  void fixturePoliciesShouldNotDependOnForbiddenLayers() {
    noClasses()
        .that()
        .resideInAnyPackage(FIXTURE_POLICY_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(FORBIDDEN_DEPENDENCY_PACKAGES)
        .check(fixtureClasses);
  }

  @Test
  void fixturePoliciesShouldNotDependOnDomainEvents() {
    noClasses()
        .that()
        .resideInAnyPackage(FIXTURE_POLICY_PACKAGE)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("com.shoestore..domain.event..")
        .check(fixtureClasses);
  }

  @Test
  void fixturePoliciesShouldNotExtendBaseEntity() {
    noClasses()
        .that()
        .resideInAnyPackage(FIXTURE_POLICY_PACKAGE)
        .should()
        .beAssignableTo(BaseEntity.class)
        .check(fixtureClasses);
  }

  @Test
  void fixturePoliciesShouldNotImplementAggregateRoot() {
    noClasses()
        .that()
        .resideInAnyPackage(FIXTURE_POLICY_PACKAGE)
        .should()
        .beAssignableTo(AggregateRoot.class)
        .check(fixtureClasses);
  }

  @Test
  void productionPolicyImplementationsShouldBeFinal() {
    classes()
        .that()
        .resideInAnyPackage(PRODUCTION_POLICY_PACKAGES)
        .and()
        .areNotInterfaces()
        .and()
        .haveSimpleNameEndingWith("Policy")
        .should()
        .haveModifier(JavaModifier.FINAL)
        .allowEmptyShould(true)
        .check(productionClasses);
  }

  @Test
  void productionPoliciesShouldNotDependOnForbiddenLayers() {
    noClasses()
        .that()
        .resideInAnyPackage(PRODUCTION_POLICY_PACKAGES)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(FORBIDDEN_DEPENDENCY_PACKAGES)
        .allowEmptyShould(true)
        .check(productionClasses);
  }

  @Test
  void productionPoliciesShouldNotDependOnDomainEvents() {
    noClasses()
        .that()
        .resideInAnyPackage(PRODUCTION_POLICY_PACKAGES)
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("com.shoestore..domain.event..")
        .allowEmptyShould(true)
        .check(productionClasses);
  }

  @Test
  void productionPoliciesShouldNotExtendBaseEntity() {
    noClasses()
        .that()
        .resideInAnyPackage(PRODUCTION_POLICY_PACKAGES)
        .should()
        .beAssignableTo(BaseEntity.class)
        .allowEmptyShould(true)
        .check(productionClasses);
  }

  @Test
  void productionPoliciesShouldNotImplementAggregateRoot() {
    noClasses()
        .that()
        .resideInAnyPackage(PRODUCTION_POLICY_PACKAGES)
        .should()
        .beAssignableTo(AggregateRoot.class)
        .allowEmptyShould(true)
        .check(productionClasses);
  }

  @Test
  void fixturePoliciesShouldNotReadSystemClockDirectly() {
    assertNoForbiddenMethodCalls(
        fixtureClasses,
        Set.of(
            "java.time.Instant#now",
            "java.time.LocalDate#now",
            "java.time.LocalDateTime#now",
            "java.time.OffsetDateTime#now",
            "java.time.ZonedDateTime#now"));
  }

  @Test
  void fixturePoliciesShouldNotGenerateUuidOrRandomValues() {
    assertNoForbiddenMethodCalls(
        fixtureClasses,
        Set.of(
            "java.util.UUID#randomUUID",
            "java.lang.Math#random",
            "java.util.concurrent.ThreadLocalRandom#current"));

    assertNoForbiddenConstructorCalls(
        fixtureClasses, Set.of("java.util.Random", "java.security.SecureRandom"));
  }

  @Test
  void productionPoliciesShouldNotReadSystemClockDirectly() {
    assertNoForbiddenMethodCalls(
        productionClasses,
        Set.of(
            "java.time.Instant#now",
            "java.time.LocalDate#now",
            "java.time.LocalDateTime#now",
            "java.time.OffsetDateTime#now",
            "java.time.ZonedDateTime#now"),
        true);
  }

  @Test
  void productionPoliciesShouldNotGenerateUuidOrRandomValues() {
    assertNoForbiddenMethodCalls(
        productionClasses,
        Set.of(
            "java.util.UUID#randomUUID",
            "java.lang.Math#random",
            "java.util.concurrent.ThreadLocalRandom#current"),
        true);

    assertNoForbiddenConstructorCalls(
        productionClasses, Set.of("java.util.Random", "java.security.SecureRandom"), true);
  }

  private static boolean residesInProductionPolicyPackage(String packageName) {

    return packageName.equals("com.shoestore.shared.domain.policy")
        || packageName.startsWith("com.shoestore.shared.domain.policy.")
        || packageName.matches("com\\.shoestore\\.modules\\.[^.]+\\.domain\\.policy(?:\\..*)?");
  }

  private static void assertNoForbiddenMethodCalls(
      JavaClasses classesToCheck, Set<String> forbiddenCalls) {

    assertNoForbiddenMethodCalls(classesToCheck, forbiddenCalls, false);
  }

  private static void assertNoForbiddenMethodCalls(
      JavaClasses classesToCheck, Set<String> forbiddenCalls, boolean productionPoliciesOnly) {

    classesToCheck.forEach(
        javaClass -> {
          if (productionPoliciesOnly
              && !residesInProductionPolicyPackage(javaClass.getPackageName())) {
            return;
          }

          for (JavaMethodCall methodCall : javaClass.getMethodCallsFromSelf()) {

            String ownerName = methodCall.getTarget().getOwner().getFullName();

            String methodName = methodCall.getTarget().getName();

            String call = ownerName + "#" + methodName;

            assertThat(call)
                .as("%s must not call %s", javaClass.getFullName(), call)
                .isNotIn(forbiddenCalls);
          }
        });
  }

  private static void assertNoForbiddenConstructorCalls(
      JavaClasses classesToCheck, Set<String> forbiddenConstructors) {

    assertNoForbiddenConstructorCalls(classesToCheck, forbiddenConstructors, false);
  }

  private static void assertNoForbiddenConstructorCalls(
      JavaClasses classesToCheck,
      Set<String> forbiddenConstructors,
      boolean productionPoliciesOnly) {

    classesToCheck.forEach(
        javaClass -> {
          if (productionPoliciesOnly
              && !residesInProductionPolicyPackage(javaClass.getPackageName())) {
            return;
          }

          for (JavaConstructorCall constructorCall : javaClass.getConstructorCallsFromSelf()) {

            String ownerName = constructorCall.getTarget().getOwner().getFullName();

            assertThat(ownerName)
                .as("%s must not call constructor %s", javaClass.getFullName(), ownerName)
                .isNotIn(forbiddenConstructors);
          }
        });
  }
}
