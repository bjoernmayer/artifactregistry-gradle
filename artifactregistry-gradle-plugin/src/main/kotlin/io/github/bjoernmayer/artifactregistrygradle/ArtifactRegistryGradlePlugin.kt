package io.github.bjoernmayer.artifactregistrygradle

import io.github.bjoernmayer.artifactregistrygradle.googleCredentialsSupplier.ApplicationDefault
import io.github.bjoernmayer.artifactregistrygradle.googleCredentialsSupplier.GCloudSDK
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.ProviderFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ArtifactRegistryGradlePlugin : Plugin<Any> {
    private lateinit var providerFactory: ProviderFactory
    private val artifactRegistryPasswordCredentialsSupplier: ArtifactRegistryPasswordCredentialsSupplier by lazy {
        ArtifactRegistryPasswordCredentialsSupplier(
            listOf(
                ApplicationDefault,
                GCloudSDK(providerFactory),
            ),
        )
    }

    override fun apply(target: Any) {
        providerFactory =
            when (target) {
                is Project -> {
                    target.providers
                }

                is Gradle -> {
                    target.rootProject.providers
                }

                is Settings -> {
                    target.providers
                }

                else -> {
                    logger.info(
                        "Failed to get access token from gcloud or Application Default Credentials due to unknown script type $target",
                    )
                    return
                }
            }

        val credentials = artifactRegistryPasswordCredentialsSupplier.get()

        when (target) {
            is Settings -> target.applyPlugin(credentials)
            is Gradle -> target.applyPlugin(credentials)
            is Project -> target.applyPlugin(credentials)
        }
    }

    private fun Settings.applyPlugin(passwordCredentials: PasswordCredentials?) {
        gradle.applyPlugin(passwordCredentials)
    }

    private fun Gradle.applyPlugin(passwordCredentials: PasswordCredentials?) {
        with(RepositoryConfigurer) {
            configureRepositories(passwordCredentials)
        }
    }

    private fun Project.applyPlugin(passwordCredentials: PasswordCredentials?) {
        with(RepositoryConfigurer) {
            afterEvaluate {
                configureRepositories(passwordCredentials)
            }
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ArtifactRegistryGradlePlugin::class.java)
    }
}
