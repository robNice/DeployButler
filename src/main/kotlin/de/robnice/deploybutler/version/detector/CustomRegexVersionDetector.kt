package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
import de.robnice.deploybutler.version.VersionDetectionResult
import java.io.File

class CustomRegexVersionDetector : VersionDetector {

    override val id: String = "custom-regex"

    override fun detect(repoRoot: File, settings: DeploySettingsState): String? {
        val path = settings.versionCustomPath.trim()
        val regexText = settings.versionCustomRegex.trim()

        if (path.isBlank() || regexText.isBlank()) return null

        val file = File(repoRoot, path)
        if (!file.isFile) return null

        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        val regex = runCatching { Regex(regexText) }.getOrNull() ?: return null
        val match = regex.find(text) ?: return null

        return match.groupValues.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    override fun detectResult(repoRoot: File, settings: DeploySettingsState): VersionDetectionResult? {
        val path = settings.versionCustomPath.trim()
        val regexText = settings.versionCustomRegex.trim()
        if (path.isBlank() || regexText.isBlank()) return null
        val file = File(repoRoot, path)
        if (!file.isFile) return null
        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        val regex = runCatching { Regex(regexText) }.getOrNull() ?: return null
        val version = regex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return VersionDetectionResult(version = version, sourceFile = file) { newVersion ->
            replaceGroupInFile(file, regex, newVersion)
        }
    }
}