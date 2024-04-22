package io.github.bjoernmayer.artifactregistrygradle.googleCredentialsSupplier

import io.mockk.every
import io.mockk.mockk
import org.gradle.api.provider.ProviderFactory
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
}
