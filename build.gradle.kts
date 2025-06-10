import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun prop(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.1"
    id("org.jetbrains.changelog") version "2.2.0"
}

group = prop("pluginGroup")
version = prop("pluginVersion")

// Configure project's dependencies
repositories {
    maven("https://www.jetbrains.com/intellij-repository/releases")
    maven("https://www.jetbrains.com/intellij-repository/snapshots")
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(prop("pluginName"))
    version.set(prop("platformVersion"))
    type.set(prop("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(prop("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}
// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(prop("pluginVersion"))
    keepUnreleasedSection.set(false)
    groups.set(emptyList())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(prop("jdkVersion")))
    }
}

tasks {
    // Set the JVM compatibility versions
    compileJava {
        options.release.set(prop("compatibleJdkVersion").toInt())
    }

    wrapper {
        gradleVersion = prop("gradleVersion")
    }

    patchPluginXml {
        pluginId.set(prop("pluginGroup"))
        version.set(prop("pluginVersion"))
        sinceBuild.set(prop("pluginSinceBuild"))
        untilBuild.set(prop("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            changelog.renderItem(
                changelog
                    .getLatest()
                    .withHeader(true)
                    .withEmptySections(false),
                Changelog.OutputType.HTML
            )
        })
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("intellijPublishToken"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(prop("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}
