package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
import de.robnice.deploybutler.version.VersionDetectionResult
import java.io.File

class ComposerVersionDetector : VersionDetector {

    override val id: String = "composer"

    private val versionRegex = Regex(""""version"\s*:\s*"([^"]+)"""")

    override fun detect(repoRoot: File, settings: DeploySettingsState): String? {
        val file = File(repoRoot, "composer.json")
        if (!file.isFile) return null
        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        return versionRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    override fun detectResult(repoRoot: File, settings: DeploySettingsState): VersionDetectionResult? {
        val file = File(repoRoot, "composer.json")
        if (!file.isFile) return null
        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        val version = versionRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return VersionDetectionResult(version = version, sourceFile = file) { newVersion ->
            replaceGroupInFile(file, versionRegex, newVersion)
        }
    }
}