package com.ballastlane.pokedex;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Enforces the Clean Architecture dependency rule at build time. */
@AnalyzeClasses(packages = "com.ballastlane.pokedex", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainDependsOnNothingOutsideItself = noClasses()
            .that().resideInAPackage("com.ballastlane.pokedex.domain..")
            .should().dependOnClassesThat().resideInAnyPackage("com.ballastlane.pokedex.application..", "com.ballastlane.pokedex.infrastructure..", "com.ballastlane.pokedex.web..");

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that().resideInAPackage("com.ballastlane.pokedex.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "com.fasterxml..", "io.jsonwebtoken..");

    @ArchTest
    static final ArchRule applicationOnlyKnowsDomain = noClasses()
            .that().resideInAPackage("com.ballastlane.pokedex.application..")
            .should().dependOnClassesThat().resideInAnyPackage("com.ballastlane.pokedex.infrastructure..", "com.ballastlane.pokedex.web..");

    @ArchTest
    static final ArchRule applicationIsFrameworkFree = noClasses()
            .that().resideInAPackage("com.ballastlane.pokedex.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "com.fasterxml..");

    @ArchTest
    static final ArchRule infrastructureDoesNotReachIntoWeb = noClasses()
            .that().resideInAPackage("com.ballastlane.pokedex.infrastructure..")
            .should().dependOnClassesThat().resideInAPackage("com.ballastlane.pokedex.web..");

    @ArchTest
    static final ArchRule webDoesNotTouchInfrastructure = noClasses()
            .that().resideInAPackage("com.ballastlane.pokedex.web..")
            .should().dependOnClassesThat().resideInAPackage("com.ballastlane.pokedex.infrastructure..");
}
