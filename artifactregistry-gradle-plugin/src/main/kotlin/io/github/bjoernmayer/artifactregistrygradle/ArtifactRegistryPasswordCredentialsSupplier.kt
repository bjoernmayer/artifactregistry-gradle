package io.github.bjoernmayer.artifactregistrygradle

import com.google.auth.oauth2.GoogleCredentials
import io.github.bjoernmayer.artifactregistrygradle.googleCredentialsSupplier.ApplicationDefault
import io.github.bjoernmayer.artifactregistrygradle.googleCredentialsSupplier.GCloudSDK
import org.gradle.api.provider.ProviderFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Instant
import java.util.function.Supplier
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * DefaultCredentialProvider fetches credentials from gcloud and falls back to Application Default
 * Credentials if that fails.
 */
internal class ArtifactRegistryPasswordCredentialsSupplier(
    providerFactory: ProviderFactory,
) : Supplier<ArtifactRegistryPasswordCredentials?> {
    private val googleCredentialsSuppliers: List<Supplier<GoogleCredentials?>> by lazy {
        listOf(
            ApplicationDefault,
            GCloudSDK(providerFactory),
        )
    }

    private val refreshInterval = 10.seconds.toJavaDuration()
    private var lastRefresh: Instant = Instant.ofEpochMilli(0)

    private val cachedCredentials: GoogleCredentials by lazy {
        logger.info("Initializing Credentials...")

        googleCredentialsSuppliers.firstNotNullOfOrNull {
            it.get()?.refreshIfNeeded()
        } ?: run {
            logger.info("ArtifactRegistry: No credentials could be found.")

            throw IOException("Failed to find credentials Check debug logs for more details.")
        }
    }

    override fun get(): ArtifactRegistryPasswordCredentials? =
        try {
            ArtifactRegistryPasswordCredentials(cachedCredentials.apply { refreshIfExpired() }.accessToken)
        } catch (e: IOException) {
            logger.info("Failed to get access token from gcloud or Application Default Credentials", e)

            null
        }

    private fun GoogleCredentials.refreshIfNeeded(): GoogleCredentials =
        apply {
            val now = Instant.now()
            if (now > lastRefresh + refreshInterval) {
                logger.info("Refreshing Credentials...")
                refreshIfExpired()
                lastRefresh = now
            }
        }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ArtifactRegistryPasswordCredentialsSupplier::class.java)
    }
}
