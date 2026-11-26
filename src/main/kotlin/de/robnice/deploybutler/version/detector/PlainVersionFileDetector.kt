package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
import de.robnice.deploybutler.version.VersionDetectionResult
import java.io.File

class PlainVersionFileDetector : VersionDetector {

    override val id: String = "version-file"

    private val candidates = listOf("VERSION", "VERSION.txt", "version.txt")

    private val versionRegex = Regex("""^\s*v?(\d+\.\d+\.\d+[^\s]*)""", RegexOption.MULTILINE)

    private fun findFile(repoRoot: File): File? =
        candidates.map { File(repoRoot, it) }.firstOrNull { it.isFile }

    private fun readVersion(file: File): String? {
        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        return versionRegex.find(text.removePrefix("\uFEFF"))?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    override fun detect(repoRoot: File, settings: DeploySettingsState): String? {
        val file = findFile(repoRoot) ?: return null
        return readVersion(file)
    }

    override fun detectResult(repoRoot: File, settings: DeploySettingsState): VersionDetectionResult? {
        val file = findFile(repoRoot) ?: return null
        val version = readVersion(file) ?: return null
        return VersionDetectionResult(version = version, sourceFile = file) { newVersion ->
            replaceGroupInFile(file, versionRegex, newVersion)
        }
    }
}
