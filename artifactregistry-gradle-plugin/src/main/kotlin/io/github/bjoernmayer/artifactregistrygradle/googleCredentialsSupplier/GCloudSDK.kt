@file:Suppress("UnstableApiUsage")

package io.github.bjoernmayer.artifactregistrygradle.googleCredentialsSupplier

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.google.api.client.util.GenericData
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.internal.ExecException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.function.Supplier

internal class GCloudSDK(
    providerFactory: ProviderFactory,
) : Supplier<GoogleCredentials?> {
    private val accessTokenSupplier by lazy {
        AccessTokenSupplier(providerFactory)
    }

    override fun get(): Credentials? {
        logger.debug("Trying gcloud credentials...")

        return try {
            Credentials(accessTokenSupplier).also {
                logger.info("Using credentials retrieved from gcloud.")
            }
        } catch (e: IOException) {
            logger.info("Failed to retrieve credentials from gcloud: " + e.message)
            null
        }
    }

    internal class Credentials internal constructor(
        private val supplier: Supplier<AccessToken>,
    ) : GoogleCredentials(
            newBuilder()
                .apply {
                    this.accessToken = supplier.get()
                },
        ) {
        override fun refreshAccessToken(): AccessToken {
            logger.info("Refreshing gcloud credentials...")

            return supplier.get()
        }

        companion object {
            private val logger: Logger = LoggerFactory.getLogger(Credentials::class.java)
        }
    }

    private class AccessTokenSupplier(
        private val providerFactory: ProviderFactory,
    ) : Supplier<AccessToken> {
        private val gCloudCommand =
            if (providerFactory.systemProperty("os.name").orNull?.startsWith("Windows") == true) {
                "gcloud.cmd"
            } else {
                "gcloud"
            }

        private val jacksonObjectMapper by lazy {
            ObjectMapper().registerModule(KotlinModule.Builder().build())
        }

        override fun get(): AccessToken {
            val execOutput =
                try {
                    providerFactory.exec {
                        it.commandLine(
                            gCloudCommand,
                            "config",
                            "config-helper",
                            "--format=json(credential)",
                        )
                    }
                } catch (e: ExecException) {
                    throw IOException(e)
                } catch (e: Exception) {
                    logger.debug("Caught exception", e)
                    throw IOException(e)
                }

            val exitCode = execOutput.result.get().exitValue
            val stdOut = execOutput.standardOutput.asText.get()

            if (exitCode != 0) {
                val stdErr = execOutput.standardError.asText.get()

                throw IOException(
                    String.format(
                        "gcloud exited with status: %d\nOutput:\n%s\nError Output:\n%s\n",
                        exitCode,
                        stdOut,
                        stdErr,
                    ),
                )
            }

            val genericData = jacksonObjectMapper.readValue(stdOut, jacksonTypeRef<GenericData>())
            val credential =
                genericData["credential"] as Map<*, *>? ?: throw IOException("No credential returned from gcloud")

            if (KEY_ACCESS_TOKEN !in credential.keys || KEY_TOKEN_EXPIRY !in credential.keys) {
                throw IOException("Malformed response from gcloud")
            }

            val expiry =
                try {
                    dateFormat.parse(credential[KEY_TOKEN_EXPIRY] as? String)
                } catch (e: ParseException) {
                    throw IOException("Failed to parse timestamp from gcloud output", e)
                }

            val tokenValue: String =
                credential[KEY_ACCESS_TOKEN] as? String ?: throw IOException(
                    "Invalid JSON Format. AccessToken was not of type string",
                )

            return AccessToken(
                tokenValue,
                expiry,
            ).validate()
        }

        /**
         * Checks that the token is valid, throws IOException if it is expired.
         * If this plugin is run when gcloud has expired auth, then it gcloud doesn't
         * throw any errors, it simply returns an expired token. We check the token
         * that is returned and throw an error if it's expired to prompt the user to
         * login.
         */
        private fun AccessToken.validate(): AccessToken =
            apply {
                val expiry = expirationTime

                if (expiry.before(Date())) {
                    throw IOException("AccessToken is expired - maybe run `gcloud auth login`")
                }
            }

        companion object {
            private const val KEY_ACCESS_TOKEN = "access_token"
            private const val KEY_TOKEN_EXPIRY = "token_expiry"

            private val dateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
        }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(GCloudSDK::class.java)
    }
}
