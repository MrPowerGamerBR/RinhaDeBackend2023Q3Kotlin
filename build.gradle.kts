plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("com.google.cloud.tools.jib") version "3.3.2"
}

group = "com.mrpowergamerbr.rinhadebackend2023q3"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.perfectdreams.net/")
}

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core:2.3.3")
    implementation("io.ktor:ktor-server-cio:2.3.3")
    implementation("net.perfectdreams.sequins.ktor:base-route:1.0.4")

    // Databases
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.3.8")
    // Exposed 0.42.2 breaks our suspendable transaction workaround (probably because they finally fixed it in 0.42.0)
    // But we need to test it a bit further, so let's just not use it yet
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")
    api("net.perfectdreams.exposedpowerutils:exposed-power-utils:1.2.1")
    api("net.perfectdreams.exposedpowerutils:postgres-power-utils:1.2.1")

    // Logging
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("ch.qos.logback:logback-classic:1.4.9")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0-RC")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

jib {
    container {
        mainClass = "com.mrpowergamerbr.rinhadebackend2023q3.RinhaDeBackend2023Q3Launcher"
    }

    to {
        image = "ghcr.io/mrpowergamerbr/rinha-de-backend-2023-q3"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        image = "eclipse-temurin:20-jammy"
    }
}