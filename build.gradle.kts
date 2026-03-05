import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
//import org.jetbrains.intellij.platform.gradle.models.ProductRelease

fun prop(key: String) = providers.gradleProperty(key).get()

plugins {
    id("java")
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
}

group = prop("pluginGroup")
version = prop("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(prop("platformVersion"))
        bundledPlugin("com.intellij.java")
    }
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellijPlatform {
    pluginConfiguration {
        name = prop("pluginName")
        version = prop("pluginVersion")
        ideaVersion {
            sinceBuild = prop("pluginSinceBuild")
        }

        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }
    }

    pluginVerification {

        ides {
            create(IntelliJPlatformType.IntellijIdea, prop("platformVersion"))
//            recommended()
//            select {
//                types = listOf(IntelliJPlatformType.IntellijIdea)
//                channels = listOf(ProductRelease.Channel.RELEASE)
//                sinceBuild = prop("pluginSinceBuild")
//            }
        }
    }

}
changelog {
    version = prop("pluginVersion")
    itemPrefix = "-"
    keepUnreleasedSection = true
    unreleasedTerm = "[Unreleased]"
    groups = listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security")
    combinePreReleases = true
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(prop("jdkVersion")))
    }
}

tasks {
    wrapper {
        gradleVersion = prop("gradleVersion")
    }

    withType<JavaCompile> {
        sourceCompatibility = prop("compatibleJdkVersion")
        targetCompatibility = prop("compatibleJdkVersion")
    }

    patchPluginXml {
        changeNotes = provider {
            changelog.renderItem(
                changelog
                    .getLatest()
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML
            )
        }
    }

    publishPlugin {
        dependsOn(patchChangelog)
        token.set(System.getenv("intellijPublishToken"))
    }
}
