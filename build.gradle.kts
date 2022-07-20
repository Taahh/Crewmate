import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
}

group = "dev.taah"
version = "0.2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.netty:netty-all:4.1.79.Final")
    implementation("org.jetbrains:annotations:20.1.0")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.projectlombok:lombok:1.18.22")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    implementation("com.google.code.gson:gson:2.9.0")
    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")

    annotationProcessor("org.projectlombok:lombok:1.18.22")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}