# Artifact Registry Gradle

[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v?label=Plugin%20Portal&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fio%2Fgithub%2Fbjoernmayer%2FartifactregistryGradlePlugin%2Fio.github.bjoernmayer.artifactregistryGradlePlugin.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/io.github.bjoernmayer.artifactregistryGradlePlugin)

This repository contains a Gradle plugin to help with interacting with Maven repositories hosted on Artifact Registry.

It is basically a copy of [Artifact Registry Maven Tools](https://github.com/GoogleCloudPlatform/artifact-registry-maven-tools) but only for Gradle and more up to date.

## Usage

Apply the plugin:
- In `build.gradle.kts` for single module projects
- In `settings.gradle.kts` for multi module projects

```kts
id("io.github.bjoernmayer.artifactregistryGradlePlugin") version "<VERSION>"
```

### Using with Jib in a multi module project
If you see weird errors like `NoSuchMethod`, you might need to pin down the guava version for your buildscript:
```kts
// root settings.gradle.kts
buildscript {
    dependencies {
        classpath("com.google.guava:guava:33.2.0-jre")
    }
}
```

## Authentication

Requests to Artifact Registry will be authenticated using credentials from the environment. The
tools described below search the environment for credentials in the following order:
1. [Google Application Default Credentials](https://developers.google.com/accounts/docs/application-default-credentials).
    * Note: It is possible to set Application Default Credentials for a user account via `gcloud auth login --update-adc` or `gcloud auth application-default login`
1. From the `gcloud` SDK. (i.e., the access token printed via `gcloud config config-helper --format='value(credential.access_token)'`)
    * Hint: You can see which account is active with the command `gcloud config config-helper --format='value(configuration.properties.core.account)'`
