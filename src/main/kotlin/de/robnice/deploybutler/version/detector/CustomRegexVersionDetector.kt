package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
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
}