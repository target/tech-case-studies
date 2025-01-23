import com.avast.gradle.dockercompose.RemoveImages

plugins {
    id("application")
    id("java")
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.docker.compose)
}

application {
    mainClass.set("com.target.retail.data.services.Main")
}

configurations {
    all {
        exclude(group = "ch.qos.logback")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

dependencies {
    annotationProcessor(libs.spring.boot.configuration.processor)

    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.log4j2)
    implementation(libs.springdoc.openapi)
    implementation(libs.jackson.csv)

    testImplementation(libs.spring.boot.starter.test)
    // webflux is needed to use WebTestClient
    testImplementation(libs.spring.boot.starter.webflux)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<Jar> {
    manifest {
        enabled = true
    }
}

tasks.test {
    useJUnitPlatform()
}

dockerCompose {
    useComposeFiles.set(listOf("docker-compose.yml"))
    startedServices.set(listOf("app"))
    stopContainers.set(true)
    removeContainers.set(true)
    removeImages.set(RemoveImages.All)
    removeVolumes.set(true)
    removeOrphans.set(false)
    captureContainersOutput.set(true)
    waitForTcpPorts.set(true)
}

tasks.register("buildAndRunDockerCompose") {
    doLast {
        exec {
            commandLine("sh", "-c", "docker-compose down")
        }
        exec {
            commandLine("sh", "-c", "./gradlew clean build")
        }
        exec {
            commandLine("sh", "-c", "docker build -t retail-data-services-app .")
        }
        exec {
            commandLine("sh", "-c", "docker-compose up")
        }
    }
}