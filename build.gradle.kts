plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.10.2"
}

group = "de.robnice"
version = "1.1.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Base IDE for development (works for ALL JetBrains IDEs)
        intellijIdea("2025.2.4")

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
              <li>Expanded package.json version detection to also check common app folders and monorepo locations such as app, frontend, apps/*, packages/*, and services/*.</li>
              <li>Reworked translations for better consistency and wording.</li>
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
