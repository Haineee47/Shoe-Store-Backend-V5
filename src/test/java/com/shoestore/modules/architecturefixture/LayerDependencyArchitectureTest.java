package com.shoestore.modules.architecturefixture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.shoestore")
class LayerDependencyArchitectureTest {

    @ArchTest
    static final ArchRule domainMustNotDependOnOuterLayers =
            noClasses()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "..application..",
                            "..infrastructure..",
                            "..presentation.."
                    );

    @ArchTest
    static final ArchRule presentationMustNotAccessPersistence =
            noClasses()
                    .that()
                    .resideInAPackage("..presentation..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..infrastructure.persistence..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule applicationMustNotDependOnPresentation =
            noClasses()
                    .that()
                    .resideInAPackage("..application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..presentation..")
                    .allowEmptyShould(true);
}
