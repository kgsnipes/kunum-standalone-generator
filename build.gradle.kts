import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.serialization") version "1.7.21"
    application
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

//    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
//    implementation("org.slf4j:slf4j-api:2.0.5")


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

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("org.kunum.AppKt")
}
