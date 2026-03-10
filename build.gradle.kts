plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.10.2"
}

group = "de.robnice"
version = "1.0.5"

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
    <li>Added automatic project version detection for release tagging.</li>
    <li>Supported version detection sources:
      <ul>
        <li>Gradle</li>
        <li>Android Gradle projects (including <code>app/build.gradle</code> and <code>app/build.gradle.kts</code>)</li>
        <li><code>package.json</code></li>
        <li><code>composer.json</code></li>
        <li><code>pom.xml</code></li>
      </ul>
    </li>
    <li>Added configurable custom version detection via file path and regular expression.</li>
    <li>Added preferred version detector setting to prioritize a specific source when multiple supported files exist.</li>
    <li>Release dialog now offers <b>Tag from project file</b> when a valid version can be detected automatically.</li>
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
