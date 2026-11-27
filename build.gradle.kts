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
              <li>Added a tag annotation field to the release dialog.</li>
              <li>Added a configurable default tag annotation (pre-filled in the release dialog, supports the {tag} placeholder).</li>
              <li>The annotation can be pre-filled from outside (e.g. by a script or CLI tool) via the file .git/DEPLOYBUTLER_ANNOTATION.</li>
              <li>Added settings to remember the last tag option, tag extension and annotation in the release dialog.</li>
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
