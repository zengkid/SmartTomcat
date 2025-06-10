fun prop(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
    // REMOVED: id("org.jetbrains.changelog") version "2.2.0" - This causes Jackson issues
}

group = prop("pluginGroup")
version = prop("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
    // Removed problematic repositories that cause Jackson conflicts
}

// NO external dependencies for Phase 1 - IntelliJ provides everything we need
dependencies {
    // Empty - Phase 1 uses only IntelliJ Platform APIs
}

// Configure Gradle IntelliJ Plugin
intellij {
    pluginName.set(prop("pluginName"))
    version.set(prop("platformVersion"))
    type.set(prop("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(
        listOf(
            *prop("platformPlugins")
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toTypedArray()
        )
    )
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
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.release.set(prop("compatibleJdkVersion").toInt())
        options.encoding = "UTF-8"
    }

    wrapper {
        gradleVersion = prop("gradleVersion")
    }

    patchPluginXml {
        pluginId.set(prop("pluginGroup"))
        version.set(prop("pluginVersion"))
        sinceBuild.set(prop("pluginSinceBuild"))
        untilBuild.set(prop("pluginUntilBuild"))

        // Simple plugin description for Phase 1 (no markdown processing to avoid dependencies)
        pluginDescription.set("""
            <h2>DevTomcat - Enhanced Tomcat Integration</h2>
            <p>Professional Tomcat plugin bringing Ultimate-like features to IntelliJ Community Edition:</p>
            <ul>
                <li><strong>Enhanced Deployment Logging</strong> - Real-time progress with Ultimate-style messages</li>
                <li><strong>Professional Console Output</strong> - Structured logging with timestamps</li>
                <li><strong>Intelligent Log Parsing</strong> - Smart analysis of Tomcat output</li>
                <li><strong>Multiple Console Tabs</strong> - Server, Catalina, and Localhost logs</li>
                <li><strong>Ultimate-Style Features</strong> - Professional deployment status feedback</li>
            </ul>
            <p>Perfect for Spring Boot, JSP, and enterprise Java development with IntelliJ Community Edition.</p>
        """)

        // Simple change notes for Phase 1 (no changelog plugin dependency)
        changeNotes.set("""
            <h3>DevTomcat ${prop("pluginVersion")} - Phase 1: Enhanced Logging</h3>
            <ul>
                <li><strong>Ultimate-Style Deployment Logging</strong> - Professional deployment messages with timestamps</li>
                <li><strong>Real-Time Progress Monitoring</strong> - Live deployment status updates</li>
                <li><strong>Enhanced Console Management</strong> - Multiple console tabs (Server, Catalina, Localhost)</li>
                <li><strong>Intelligent Log Parsing</strong> - Smart analysis of Tomcat server output</li>
                <li><strong>Professional Status Feedback</strong> - Enhanced deployment success/failure reporting</li>
                <li><strong>Community Edition Compatible</strong> - All Ultimate-like features for free</li>
            </ul>
            <p><em>Phase 2 Coming Soon: JMX integration, hot deployment, and advanced configuration UI</em></p>
        """)
    }

    publishPlugin {
        token.set(System.getenv("intellijPublishToken"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(prop("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }

    // Disable problematic tasks that can cause issues during development
    named("buildSearchableOptions") {
        enabled = false
    }

    test {
        // Add JVM arguments to fix Java 17 module access issues
        jvmArgs(
            "--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens", "java.desktop/java.awt.event=ALL-UNNAMED",
            "--add-opens", "java.desktop/java.awt=ALL-UNNAMED",
            "--add-opens", "java.desktop/javax.swing=ALL-UNNAMED",
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.util=ALL-UNNAMED"
        )

        useJUnit()

        // Set system properties for IntelliJ testing
        systemProperty("idea.test.cyclic.buffer.size", "1048576")
        systemProperty("file.encoding", "UTF-8")

        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }
}