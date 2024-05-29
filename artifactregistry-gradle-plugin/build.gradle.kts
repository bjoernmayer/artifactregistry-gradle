plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.23"

    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("com.gradle.plugin-publish") version "1.2.1"
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
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")    // https://mvnrepository.com/artifact/com.google.auth/google-auth-library-oauth2-http
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0") // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("io.mockk:mockk:1.13.10")
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
version = "0.2.1"

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

tasks.test {
    useJUnitPlatform()
}
