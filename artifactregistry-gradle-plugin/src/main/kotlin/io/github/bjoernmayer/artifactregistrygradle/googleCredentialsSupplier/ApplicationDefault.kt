package io.github.bjoernmayer.artifactregistrygradle.googleCredentialsSupplier

import com.google.auth.oauth2.GoogleCredentials
import org.gradle.api.provider.ProviderFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.function.Supplier

internal class ApplicationDefault(
    private val providerFactory: ProviderFactory,
) : Supplier<GoogleCredentials?> {
    private val logger: Logger = LoggerFactory.getLogger(ApplicationDefault::class.java)

    private val scopes =
        arrayOf(
            "https://www.googleapis.com/auth/cloud-platform",
            "https://www.googleapis.com/auth/cloud-platform.read-only",
        )

    override fun get(): GoogleCredentials? {
        logger.debug("Trying Application Default Credentials...")

        return try {
            val credentialsPath =
                providerFactory
                    .environmentVariable(APPLICATION_CREDENTIALS_ENV_VAR_NAME)
                    .orNull
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IOException("GOOGLE_APPLICATION_CREDENTIALS Env Var not set or empty")

            val credentialsInputStream = FileInputStream(File(credentialsPath))

            GoogleCredentials
                .fromStream(credentialsInputStream)
                .createScoped(*scopes)
                .also { logger.info("Using Application Default Credentials.") }
        } catch (e: IOException) {
            logger.info("Application Default Credentials unavailable.")
            logger.debug("Failed to retrieve Application Default Credentials: " + e.message)

            null
        }
    }

    private companion object {
        const val APPLICATION_CREDENTIALS_ENV_VAR_NAME = "GOOGLE_APPLICATION_CREDENTIALS"
    }
}
