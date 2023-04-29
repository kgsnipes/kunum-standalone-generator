

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    application
    jacoco
    id("org.sonarqube") version "3.5.0.2730"
}

group = "org.kunum"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/io.github.microutils/kotlin-logging
    implementation("io.github.microutils:kotlin-logging:3.0.4")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-jdk8
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")

    implementation("io.javalin:javalin:5.2.0")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.4.5")

    implementation("com.zaxxer:HikariCP:4.0.3")

    // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    implementation("org.xerial:sqlite-jdbc:3.40.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    // https://mvnrepository.com/artifact/info.picocli/picocli
    implementation("info.picocli:picocli:4.7.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}



tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

//
//kotlin{
//    jvmToolchain(8)
//}

application {
    mainClass.set("org.kunum.AppKt")
}

jacoco {
    toolVersion = "0.8.8"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}
//
//
//sonar {
//    properties {
//        property("sonar.projectKey", "kgsnipes_kunum-standalone-generator")
//        property("sonar.organization", "kgsnipes")
//        property("sonar.host.url", "https://sonarcloud.io")
//    }
//}
