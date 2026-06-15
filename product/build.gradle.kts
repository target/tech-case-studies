application {
    mainClass.set("com.target.retail.product.Main")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("product.jar")
}

dependencies {
    annotationProcessor(libs.spring.boot.configuration.processor)

    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.log4j2)
    implementation(libs.springdoc.openapi)
    implementation(libs.jackson.csv)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webflux)
}
