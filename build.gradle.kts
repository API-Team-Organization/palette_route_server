import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"

    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.4"
}

val springCloudAzureVersion by extra("5.13.0")
val springAiVersion by extra("1.0.0-M6")

group = "com.teamapi"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://repo.spring.io/milestone")
}

dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux") {
        exclude("org.springframework.boot", "spring-boot-starter-json")
    }

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.0.1")
    implementation("org.mongodb:bson-kotlinx:5.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    implementation("org.springframework.ai:spring-ai-anthropic-spring-boot-starter")
    implementation(group =  "io.netty", name = "netty-resolver-dns-native-macos", classifier = "osx-aarch_64")
    implementation("com.azure.spring:spring-cloud-azure-starter-storage-blob")
//    implementation("com.azure.spring:spring-cloud-azure-starter-storage")

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-api:2.5.0")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")

}

dependencyManagement {
    imports {
        mavenBom("com.azure.spring:spring-cloud-azure-dependencies:$springCloudAzureVersion")
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
