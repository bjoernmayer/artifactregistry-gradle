package io.github.bjoernmayer.artifactregistrygradle

import io.github.bjoernmayer.artifactregistrygradle.googleCredentialsSupplier.ApplicationDefault
import io.github.bjoernmayer.artifactregistrygradle.googleCredentialsSupplier.GCloudSDK
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ProviderFactory

public class ArtifactRegistryGradlePlugin
    @javax.inject.Inject
    constructor(
        private val objectFactory: ObjectFactory,
        private val providerFactory: ProviderFactory,
    ) : Plugin<ExtensionAware> {
        private lateinit var extension: ArtifactRegistryGradleExtension

        override fun apply(target: ExtensionAware) {
            extension =
                target.extensions.create(
                    "artifactRegistry",
                    ArtifactRegistryGradleExtension::class.java,
                    objectFactory,
                )

            val artifactRegistryPasswordCredentialsSupplier =
                ArtifactRegistryPasswordCredentialsSupplier().apply {
                    addSupplier(
                        extension.gCloudSDKExtension.enable,
                        extension.gCloudSDKExtension.order,
                        GCloudSDK(providerFactory),
                    )

                    addSupplier(
                        extension.applicationDefaultExtension.enable,
                        extension.applicationDefaultExtension.order,
                        ApplicationDefault(providerFactory),
                    )
                }

            when (target) {
                is Project -> target.applyPlugin(artifactRegistryPasswordCredentialsSupplier)
                is Gradle -> target.applyPlugin(artifactRegistryPasswordCredentialsSupplier)
                is Settings -> target.applyPlugin(artifactRegistryPasswordCredentialsSupplier)
            }
        }

        private fun Settings.applyPlugin(passwordCredentialsSupplier: ArtifactRegistryPasswordCredentialsSupplier) {
            gradle.applyPlugin(passwordCredentialsSupplier)
        }

        private fun Gradle.applyPlugin(passwordCredentialsSupplier: ArtifactRegistryPasswordCredentialsSupplier) {
            with(RepositoryConfigurer) {
                configureRepositories(passwordCredentialsSupplier)
            }
        }

        private fun Project.applyPlugin(passwordCredentialsSupplier: ArtifactRegistryPasswordCredentialsSupplier) {
            with(RepositoryConfigurer) {
                afterEvaluate {
                    configureRepositories(passwordCredentialsSupplier)
                }
            }
        }
    }
