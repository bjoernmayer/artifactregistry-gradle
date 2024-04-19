plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.23"

    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

kotlin {
    jvmToolchain(21)
}

ktlint {
    version.set("1.2.1")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
}

gradlePlugin {
    val artifactregistryGradlePlugin by plugins.creating {
        id = "com.github.bjoernmayer.artifactregistryGradlePlugin"
        implementationClass = "com.github.bjoernmayer.artifactregistrygradle.ArtifactRegistryGradlePlugin"
    }
}
