package com.bansaiyai.bansaiyai;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.bansaiyai.bansaiyai", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule layered_architecture_is_respected = layeredArchitecture()
            .consideringOnlyDependenciesInAnyPackage("com.bansaiyai.bansaiyai..")
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .layer("Config").definedBy("..config..")
            .layer("Security").definedBy("..security..")

            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Config", "Security")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service", "Config", "Security");

    @ArchTest
    static final ArchRule controllers_should_have_controller_suffix = classes()
            .that().resideInAPackage("..controller..")
            .should().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule services_should_have_service_suffix = classes()
            .that().resideInAPackage("..service..")
            .and().areAnnotatedWith(org.springframework.stereotype.Service.class)
            .should().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule no_cycles = com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
            .matching("com.bansaiyai.bansaiyai.(*)..")
            .should().beFreeOfCycles();
}
