rootProject.name = "tech-case-studies"

include("retail-data-services")
include("cart-service")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
