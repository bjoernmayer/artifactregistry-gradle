# Artifact Registry Gradle

This repository contains a Gradle plugin to help with interacting with Maven repositories hosted on Artifact Registry.

It is basically a copy of [Artifact Registry Maven Tools](https://github.com/GoogleCloudPlatform/artifact-registry-maven-tools) but only for Gradle
and more up to date.

## Authentication

Requests to Artifact Registry will be authenticated using credentials from the environment. The
tools described below search the environment for credentials in the following order:
1. [Google Application Default Credentials](https://developers.google.com/accounts/docs/application-default-credentials).
    * Note: It is possible to set Application Default Credentials for a user account via `gcloud auth login --update-adc` or `gcloud auth application-default login`
1. From the `gcloud` SDK. (i.e., the access token printed via `gcloud config config-helper --format='value(credential.access_token)'`)
    * Hint: You can see which account is active with the command `gcloud config config-helper --format='value(configuration.properties.core.account)'`
