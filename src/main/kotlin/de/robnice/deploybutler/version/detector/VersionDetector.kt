package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
import de.robnice.deploybutler.version.VersionDetectionResult
import java.io.File

interface VersionDetector {
    val id: String
    fun detect(repoRoot: File, settings: DeploySettingsState): String?
    fun detectResult(repoRoot: File, settings: DeploySettingsState): VersionDetectionResult? = null

    fun replaceGroupInFile(file: File, regex: Regex, newVersion: String): Boolean {
        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return false
        val match = regex.find(text) ?: return false
        val group = match.groups[1] ?: return false
        val newText = text.substring(0, group.range.first) + newVersion + text.substring(group.range.last + 1)
        return runCatching { file.writeText(newText, Charsets.UTF_8) }.isSuccess
    }
}