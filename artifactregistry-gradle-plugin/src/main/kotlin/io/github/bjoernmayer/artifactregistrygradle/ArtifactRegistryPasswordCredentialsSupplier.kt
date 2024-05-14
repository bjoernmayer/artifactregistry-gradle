package io.github.bjoernmayer.artifactregistrygradle

import com.google.auth.oauth2.GoogleCredentials
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Instant
import java.util.function.Supplier
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * DefaultCredentialProvider tries to get Application Default and falls back to fetching form gcloud.
 * This Supplier goes through the list of provided [googleCredentialsSuppliers] and returns the result of the first,
 * that is not `null`. Otherwise, it returns `null`.
 */
internal class ArtifactRegistryPasswordCredentialsSupplier(
    private val googleCredentialsSuppliers: List<Supplier<GoogleCredentials?>>,
) : Supplier<ArtifactRegistryPasswordCredentials?> {
    private val refreshInterval = 10.seconds.toJavaDuration()
    private var lastRefresh: Instant = Instant.ofEpochMilli(0)

    private val googleCredentials: GoogleCredentials by lazy {
        logger.info("Initializing Credentials...")

        googleCredentialsSuppliers.firstNotNullOfOrNull {
            it.get()?.refreshIfNeeded()
        } ?: run {
            logger.info("ArtifactRegistry: No credentials could be found.")

            throw IOException("Failed to find credentials. Check debug logs for more details.")
        }
    }

    override fun get(): ArtifactRegistryPasswordCredentials? =
        try {
            ArtifactRegistryPasswordCredentials(googleCredentials.apply { refreshIfExpired() }.accessToken)
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
