import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
}


group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}


repositories {
    // 本地 WebStorm 离线仓库（跳过 933MB 下载）
    maven { url = uri("file:///home/demo/jetbrains-mirror/local-maven") }
    // 阿里云
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/central") }
    // 腾讯云
    maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
    // 华为云
    maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
    // 清华 TUNA
    maven { url = uri("https://mirrors.tuna.tsinghua.edu.cn/maven/") }
    // 中科大
    maven { url = uri("https://mirrors.ustc.edu.cn/maven/") }
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}


dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.opentest4j)
    intellijPlatform {
        webstorm("2024.2")
        bundledPlugin("JavaScript")
        bundledPlugin("org.jetbrains.plugins.sass")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("version")

        val changelog = project.changelog // local variable for configuration cache compatibility

        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels = providers.gradleProperty("pluginVersion")
            .map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}


changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}
