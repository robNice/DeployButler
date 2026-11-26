plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.10.2"
}

group = "de.robnice"
version = "1.6.2"

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
              <li>Added automatic version detection from plain VERSION files (VERSION, VERSION.txt, version.txt) in the project root.</li>
              <li>Added a tag annotation field to the release dialog. If left empty, the default annotation "Deploy &lt;tag&gt;" is used.</li>
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
