package io.github.bjoernmayer.artifactregistrygradle.googleCredentialsSupplier

import io.mockk.every
import io.mockk.mockk
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOutput
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GCloudSDKTest {
    @Test
    fun `test gcloud command throws exec exception, get should return null`() {
        val providerFactory: ProviderFactory =
            mockk {
                every {
                    exec(any())
                } throws ExecException("A problem occurred starting process 'command 'gcloud'")
            }

        assertEquals(null, GCloudSDK(providerFactory).get())
    }

    @Test
    fun `test returns null with invalid json`() {
        val providerFactory: ProviderFactory =
            mockk {
                every {
                    exec(any())
                } returns
                    object : ExecOutput {
                        override fun getResult(): Provider<ExecResult> =
                            mockk {
                                every {
                                    get()
                                } returns
                                    object : ExecResult {
                                        override fun getExitValue(): Int = 0

                                        override fun assertNormalExitValue(): ExecResult = this

                                        override fun rethrowFailure(): ExecResult = this
                                    }
                            }

                        override fun getStandardOutput(): ExecOutput.StandardStreamContent =
                            mockk {
                                every { asText } returns
                                    mockk {
                                        every {
                                            get()
                                        } returns "{}"
                                    }
                            }

                        override fun getStandardError(): ExecOutput.StandardStreamContent = mockk {}
                    }
            }

        assertEquals(null, GCloudSDK(providerFactory).get())
    }

    @Test
    fun `test returns null with no json`() {
        val providerFactory: ProviderFactory =
            mockk {
                every {
                    exec(any())
                } returns
                    object : ExecOutput {
                        override fun getResult(): Provider<ExecResult> =
                            mockk {
                                every {
                                    get()
                                } returns
                                    object : ExecResult {
                                        override fun getExitValue(): Int = 0

                                        override fun assertNormalExitValue(): ExecResult = this

                                        override fun rethrowFailure(): ExecResult = this
                                    }
                            }

                        override fun getStandardOutput(): ExecOutput.StandardStreamContent =
                            mockk {
                                every { asText } returns
                                    mockk {
                                        every {
                                            get()
                                        } returns "this is not json"
                                    }
                            }

                        override fun getStandardError(): ExecOutput.StandardStreamContent = mockk {}
                    }
            }

        assertEquals(null, GCloudSDK(providerFactory).get())
    }
}
