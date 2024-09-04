import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
}


repositories {
    maven { setUrl("https://maven.aliyun.com/repository/central") }
    maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
    maven { setUrl("https://maven.aliyun.com/repository/google") }
    maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { setUrl("https://maven.aliyun.com/repository/public") }
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://maven.aliyun.com/nexus/content/groups/public/") }
    maven { setUrl("https://maven.aliyun.com/nexus/content/repositories/jcenter") }
    google()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
    gradlePluginPortal()
}


// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.junit)

    intellijPlatform {
        webstorm("2024.2")
        bundledPlugin("JavaScript")
        instrumentationTools()
        testFramework(TestFrameworkType.Platform)
    }
}
