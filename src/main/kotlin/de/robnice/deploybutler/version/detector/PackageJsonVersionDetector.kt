package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
import java.io.File

class PackageJsonVersionDetector : VersionDetector {

    override val id: String = "package-json"

    private val versionRegex = Regex(""""version"\s*:\s*"([^"]+)"""")

    override fun detect(repoRoot: File, settings: DeploySettingsState): String? {
        val file = File(repoRoot, "package.json")
        if (!file.isFile) return null

        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        return versionRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }
}