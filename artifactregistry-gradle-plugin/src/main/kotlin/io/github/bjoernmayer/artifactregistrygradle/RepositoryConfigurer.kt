package io.github.bjoernmayer.artifactregistrygradle

import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository
import org.gradle.api.invocation.Gradle
import org.gradle.api.publish.PublishingExtension
import org.gradle.internal.authentication.DefaultBasicAuthentication
import java.net.URI
import java.net.URISyntaxException

internal object RepositoryConfigurer {
    fun Gradle.configureRepositories(passwordCredentialsSupplier: ArtifactRegistryPasswordCredentialsSupplier) {
        settingsEvaluated {
            it.configureRepositories(passwordCredentialsSupplier)
        }
        projectsLoaded {
            allprojects {
                it.buildscript.configureRepositories(passwordCredentialsSupplier)
            }
        }
        projectsEvaluated {
            allprojects {
                it.configureRepositories(passwordCredentialsSupplier)
            }
        }
    }

    fun Project.configureRepositories(passwordCredentialsSupplier: ArtifactRegistryPasswordCredentialsSupplier) {
        repositories.forEach {
            it.configure(passwordCredentialsSupplier)
        }

        extensions.findByType(PublishingExtension::class.java)?.repositories?.forEach {
            it.configure(passwordCredentialsSupplier)
        }
    }

    /**
     * Ensure repos are configured in buildscript
     */
    private fun ScriptHandler.configureRepositories(passwordCredentialsSupplier: ArtifactRegistryPasswordCredentialsSupplier) {
        repositories.whenObjectAdded {
            it.configure(passwordCredentialsSupplier)
        }
    }

    private fun Settings.configureRepositories(passwordCredentialsSupplier: ArtifactRegistryPasswordCredentialsSupplier) {
        pluginManagement.repositories.forEach {
            it.configure(passwordCredentialsSupplier)
        }
    }

    private fun ArtifactRepository.configure(passwordCredentialsSupplier: ArtifactRegistryPasswordCredentialsSupplier) {
        if (this !is DefaultMavenArtifactRepository) {
            return
        }

        if (url.scheme == null || url.scheme != "artifactregistry") {
            return
        }

        try {
            url = URI("https", url.host, url.path, url.fragment)
        } catch (e: URISyntaxException) {
            throw ProjectConfigurationException(
                String.format("Invalid repository URL %s", url.toString()),
                e,
            )
        }

        if (configuredCredentials.isPresent.not()) {
            val passwordCredentials = passwordCredentialsSupplier.get()

            passwordCredentials?.run {
                setConfiguredCredentials(this)
                authentication {
                    it.add(DefaultBasicAuthentication("basic"))
                }
            }
        }
    }
}
