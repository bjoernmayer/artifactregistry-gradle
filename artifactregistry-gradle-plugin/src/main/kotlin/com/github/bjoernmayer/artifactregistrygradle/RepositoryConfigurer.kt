package com.github.bjoernmayer.artifactregistrygradle

import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository
import org.gradle.api.invocation.Gradle
import org.gradle.api.publish.PublishingExtension
import org.gradle.internal.authentication.DefaultBasicAuthentication
import java.net.URI
import java.net.URISyntaxException

internal object RepositoryConfigurer {
    fun Gradle.configureRepositories(passwordCredentials: PasswordCredentials?) {
        settingsEvaluated {
            it.configureRepositories(passwordCredentials)
        }
        projectsLoaded {
            allprojects {
                it.buildscript.configureRepositories(passwordCredentials)
            }
        }
        projectsEvaluated {
            allprojects {
                it.configureRepositories(passwordCredentials)
            }
        }
    }

    fun Project.configureRepositories(passwordCredentials: PasswordCredentials?) {
        repositories.forEach {
            it.configure(passwordCredentials)
        }

        extensions.findByType(PublishingExtension::class.java)?.repositories?.forEach {
            it.configure(passwordCredentials)
        }
    }

    /**
     * Ensure repos are configured in buildscript
     */
    private fun ScriptHandler.configureRepositories(passwordCredentials: PasswordCredentials?) {
        repositories.whenObjectAdded {
            it.configure(passwordCredentials)
        }
    }

    private fun Settings.configureRepositories(passwordCredentials: PasswordCredentials?) {
        pluginManagement.repositories.forEach {
            it.configure(passwordCredentials)
        }
    }

    private fun ArtifactRepository.configure(passwordCredentials: PasswordCredentials?) {
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
            passwordCredentials?.run {
                setConfiguredCredentials(this)
                authentication {
                    it.add(DefaultBasicAuthentication("basic"))
                }
            }
        }
    }
}
