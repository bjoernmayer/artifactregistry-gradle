plugins {
    idea
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.23"

    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("com.gradle.plugin-publish") version "1.2.1"
}

kotlin {
    explicitApi()

    jvmToolchain(21)
}

ktlint {
    version.set("1.4.1")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    // https://mvnrepository.com/artifact/com.google.auth/google-auth-library-oauth2-http
    implementation("com.google.auth:google-auth-library-oauth2-http:1.29.0")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.1")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    testImplementation("io.mockk:mockk:1.13.13")
}

// Make sure to use guava jre
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.google.guava" && requested.version?.contains("-android") == true) {
            useVersion(requested.version!!.replace("-android", "-jre"))
        }
    }
}

group = "io.github.bjoernmayer"
version = "0.5.0"

gradlePlugin {
    website = "https://github.com/bjoernmayer/artifactregistry-gradle"
    vcsUrl = "https://github.com/bjoernmayer/artifactregistry-gradle"

    val artifactregistryGradlePlugin by plugins.creating {
        id = "io.github.bjoernmayer.artifactregistryGradlePlugin"
        implementationClass = "io.github.bjoernmayer.artifactregistrygradle.ArtifactRegistryGradlePlugin"

        displayName = "ArtifactRegistry Gradle Plugin"
        description = "Automatically handle authentication with Maven repositories hosted on Artifact Registry."
        tags = listOf("maven", "artifact", "repositories", "googleCloud")
    }
}

idea {
    // Going through decompiled class files is no fun (no code navigation),
    // hence, we instruct Gradle to download the actual sources
    module {
        isDownloadJavadoc = false
        isDownloadSources = true
    }
}

tasks.test {
    useJUnitPlatform()
}
