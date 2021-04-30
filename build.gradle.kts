import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.4.32"
    id("org.openjfx.javafxplugin") version "0.0.9"
}

group = "ru.spbu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))

    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("no.tornado:tornadofx:1.7.20")
}

application {
    mainClass.set("ru.spbu.netter.MainApp")
}

javafx {
    version = "11.0.2"
    modules("javafx.controls")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    test {
        useJUnitPlatform()
    }
}
