package com.github.bjoernmayer.artifactregistrygradle.googleCredentialsSupplier

import com.google.auth.oauth2.GoogleCredentials
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.function.Supplier

internal object ApplicationDefault : Supplier<GoogleCredentials?> {
    private val logger: Logger = LoggerFactory.getLogger(ApplicationDefault::class.java)

    private val scopes =
        arrayOf(
            "https://www.googleapis.com/auth/cloud-platform",
            "https://www.googleapis.com/auth/cloud-platform.read-only",
        )

    override fun get(): GoogleCredentials? {
        logger.debug("Trying Application Default Credentials...")

        return try {
            GoogleCredentials
                .getApplicationDefault()
                .createScoped(*scopes)
                .also { logger.info("Using Application Default Credentials.") }
        } catch (e: IOException) {
            logger.info("Application Default Credentials unavailable.")
            logger.debug("Failed to retrieve Application Default Credentials: " + e.message)

            null
        }
    }
}
