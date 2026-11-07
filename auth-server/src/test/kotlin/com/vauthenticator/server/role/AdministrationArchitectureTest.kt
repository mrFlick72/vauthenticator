package com.vauthenticator.server.role

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * The administration rule (ADR 0017) is checked in the use cases, so nothing at runtime stops a writer that forgets
 * the check: this test does, at build time.
 */
class AdministrationArchitectureTest {

    private val mainClasses: JavaClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("com.vauthenticator.server")

    @Test
    fun `every class that writes accounts checks administration`() {
        assertEveryWriterChecks(
            repository = "com.vauthenticator.server.account.domain.AccountRepository",
            writes = setOf("save", "create"),
            check = "com.vauthenticator.server.account.domain.AccountAdministration"
        )
    }

    @Test
    fun `every class that writes client applications checks administration`() {
        assertEveryWriterChecks(
            repository = "com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository",
            writes = setOf("save", "delete"),
            check = "com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationAdministration"
        )
    }

    @Test
    fun `only the security context and tenant setup decide who is an administrator`() {
        val implementations = mainClasses
            .filter { it.isAssignableTo("com.vauthenticator.server.role.domain.Administration") && !it.isInterface }
            .map { it.name }

        assertThat(implementations).containsExactlyInAnyOrder(
            "com.vauthenticator.server.role.adapter.security.SecurityContextAdministration",
            "com.vauthenticator.server.management.TenantSetUpAdministration"
        )
    }

    private fun assertEveryWriterChecks(repository: String, writes: Set<String>, check: String) {
        val writers = classesCalling { it.owner.isAssignableTo(repository) && it.name in writes }
            .filterNot { isExempt(it) }
        val checkers = classesCalling { it.owner.name == check }

        assertThat(writers).describedAs("writers of $repository").isNotEmpty
        assertThat(writers - checkers).describedAs("writers of $repository that do not call $check").isEmpty()
    }

    // a lambda or a nested class belongs to the class it is written in
    private fun classesCalling(target: (com.tngtech.archunit.core.domain.AccessTarget.CodeUnitCallTarget) -> Boolean) =
        mainClasses
            .filter { javaClass -> javaClass.methodCallsFromSelf.any { target(it.target) } }
            .map { topLevelNameOf(it) }
            .toSet()

    private fun topLevelNameOf(javaClass: JavaClass) = javaClass.name.substringBefore('$')

    // repository implementations and decorators delegate writes; tenant setup creates the first Administrators
    private fun isExempt(className: String) =
        className.contains(".adapter.") || className.startsWith("com.vauthenticator.server.management.init.")
}
