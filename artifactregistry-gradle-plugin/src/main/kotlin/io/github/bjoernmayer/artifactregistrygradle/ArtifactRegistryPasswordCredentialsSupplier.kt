package io.github.bjoernmayer.artifactregistrygradle

import com.google.auth.oauth2.GoogleCredentials
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.gradle.api.provider.Property
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.function.Supplier
import kotlin.time.Duration.Companion.seconds

internal class ArtifactRegistryPasswordCredentialsSupplier : Supplier<ArtifactRegistryPasswordCredentials?> {
    private val potentialGoogleCredentialsSuppliers: MutableList<GoogleCredentialsSupplierEntry> = mutableListOf()

    private val refreshInterval = 10.seconds
    private var lastRefresh: Instant = Instant.fromEpochSeconds(0)

    private val googleCredentials: GoogleCredentials by lazy {
        logger.info("Initializing Credentials...")

        val googleCredentialsSuppliers =
            buildMap<Supplier<GoogleCredentials?>, Byte> {
                potentialGoogleCredentialsSuppliers.forEach {
                    if (!it.enabled.get()) {
                        return@forEach
                    }

                    put(it.supplier, it.order.get())
                }
            }.entries.sortedBy { it.value }.map { it.key }

        googleCredentialsSuppliers.firstNotNullOfOrNull {
            it.get()?.refreshIfNeeded()
        } ?: run {
            logger.info("ArtifactRegistry: No credentials could be found.")

            throw IOException("Failed to find credentials. Check debug logs for more details.")
        }
    }

    fun addSupplier(
        enabled: Property<Boolean>,
        order: Property<Byte>,
        supplier: Supplier<GoogleCredentials?>,
    ) {
        potentialGoogleCredentialsSuppliers.add(
            GoogleCredentialsSupplierEntry(enabled, order, supplier),
        )
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
            val now = Clock.System.now()
            if (now > lastRefresh + refreshInterval) {
                logger.info("Refreshing Credentials...")
                refreshIfExpired()
                lastRefresh = now
            }
        }

    private data class GoogleCredentialsSupplierEntry(
        val enabled: Property<Boolean>,
        val order: Property<Byte>,
        val supplier: Supplier<GoogleCredentials?>,
    )

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ArtifactRegistryPasswordCredentialsSupplier::class.java)
    }
}
