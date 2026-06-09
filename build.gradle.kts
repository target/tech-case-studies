plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "application")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    group = "com.target.retail"

    configurations.all {
        exclude(group = "ch.qos.logback")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<Jar> {
        manifest {
            enabled = true
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
