package io.github.bjoernmayer.artifactregistrygradle

import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.internal.impldep.org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import java.util.function.Supplier

class ArtifactRegistryPasswordCredentialsSupplierTest {
    @Test
    fun `test list of provided suppliers is iterated, second supplier should return`() {
        val firstSupplier: Supplier<GoogleCredentials?> =
            mockk {
                every { get() } returns null
            }
        val tokenValue = "thisIsSomeSuperSpecialToken"
        val secondSupplier =
            Supplier {
                GoogleCredentials
                    .newBuilder()
                    .apply {
                        accessToken =
                            AccessToken(
                                tokenValue,
                                Date.from(
                                    LocalDate
                                        .now()
                                        .plusDays(1)
                                        .atStartOfDay()
                                        .toInstant(ZoneOffset.UTC),
                                ),
                            )
                    }.build()
            }

        val instance =
            ArtifactRegistryPasswordCredentialsSupplier().apply {
                addSupplier(
                    enabled =
                        mockk {
                            every { get() } returns true
                        },
                    order =
                        mockk {
                            every { get() } returns 1
                        },
                    supplier = firstSupplier,
                )
                addSupplier(
                    enabled =
                        mockk {
                            every { get() } returns true
                        },
                    order =
                        mockk {
                            every { get() } returns 2
                        },
                    supplier = secondSupplier,
                )
            }

        val result = instance.get()

        verify { firstSupplier.get() }

        assertEquals(tokenValue, result!!.password)
    }

    @Test
    fun `test list of provided suppliers is iterated, first supplier should return`() {
        val tokenValue = "thisIsSomeSuperSpecialToken"
        val firstSupplier =
            Supplier {
                GoogleCredentials
                    .newBuilder()
                    .apply {
                        accessToken =
                            AccessToken(
                                tokenValue,
                                Date.from(
                                    LocalDate
                                        .now()
                                        .plusDays(1)
                                        .atStartOfDay()
                                        .toInstant(ZoneOffset.UTC),
                                ),
                            )
                    }.build()
            }
        val secondSupplier: Supplier<GoogleCredentials?> =
            mockk {
                every { get() } returns null
            }

        val instance =
            ArtifactRegistryPasswordCredentialsSupplier().apply {
                addSupplier(
                    enabled =
                        mockk {
                            every { get() } returns true
                        },
                    order =
                        mockk {
                            every { get() } returns 1
                        },
                    supplier = firstSupplier,
                )
                addSupplier(
                    enabled =
                        mockk {
                            every { get() } returns true
                        },
                    order =
                        mockk {
                            every { get() } returns 2
                        },
                    supplier = secondSupplier,
                )
            }

        val result = instance.get()

        assertEquals(tokenValue, result!!.password)
    }
}
