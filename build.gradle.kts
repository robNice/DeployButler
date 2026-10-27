plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.10.2"
}

group = "de.robnice"
version = "1.4.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Build against IntelliJ IDEA Community; the plugin itself additionally requires Git4Idea.
        intellijIdeaCommunity("2025.2.4")

        // Git support
        bundledPlugin("Git4Idea")

        // Test framework
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "de.robnice.deploybutler"
        name = "DeployButler"

        ideaVersion {
            sinceBuild = "252"
        }

        changeNotes = """
          <h2>New</h2>
            <ul>
              <li>Added automatic version detection for Tauri projects, including Tauri config files and Cargo.toml fallback.</li>
              <li>Added configurable tag extensions: select a suffix in the release dialog to append it to the tag with a hyphen (e.g. v1.0.1-GT).</li>
              <li>Tags with a suffix (e.g. v1.0.1-GT) are now considered when determining the latest version.</li>
            </ul>
          <h2>Fixes</h2>
            <ul>
              <li>Removed usage of deprecated platform API.</li>
            </ul>
        """.trimIndent()
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
