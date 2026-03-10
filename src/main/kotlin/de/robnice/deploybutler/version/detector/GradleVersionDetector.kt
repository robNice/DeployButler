package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
import java.io.File

class GradleVersionDetector : VersionDetector {

    override val id: String = "gradle"

    private val versionRegex = Regex(
        """\b(?:versionName|version)\s*(?:=)?\s*["']([^"']+)["']"""
    )

    override fun detect(repoRoot: File, settings: DeploySettingsState): String? {
        val candidates = buildList {
            settings.versionCustomPath.trim().takeIf { it.isNotBlank() }?.let { add(File(repoRoot, it)) }

            add(File(repoRoot, "build.gradle.kts"))
            add(File(repoRoot, "build.gradle"))
            add(File(repoRoot, "app/build.gradle.kts"))
            add(File(repoRoot, "app/build.gradle"))
        }

        return candidates
            .distinctBy { it.absolutePath }
            .firstNotNullOfOrNull { file ->
                extractVersion(file)
            }
    }

    private fun extractVersion(file: File): String? {
        if (!file.isFile) return null
        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        return versionRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }
}