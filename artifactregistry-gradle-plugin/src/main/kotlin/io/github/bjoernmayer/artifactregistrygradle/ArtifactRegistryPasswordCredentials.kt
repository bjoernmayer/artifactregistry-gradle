package io.github.bjoernmayer.artifactregistrygradle

import com.google.auth.oauth2.AccessToken
import org.gradle.api.credentials.PasswordCredentials

internal class ArtifactRegistryPasswordCredentials(
    private val accessToken: AccessToken,
) : PasswordCredentials {
    private val username: String = "oauth2accesstoken"

    override fun getUsername(): String = username

    override fun setUsername(userName: String?) {
        throw NotImplementedError()
    }

    override fun getPassword(): String = accessToken.tokenValue

    override fun setPassword(password: String?) {
        throw NotImplementedError()
    }
}
