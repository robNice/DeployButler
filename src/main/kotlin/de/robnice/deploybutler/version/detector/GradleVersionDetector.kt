package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
import de.robnice.deploybutler.version.VersionDetectionResult
import java.io.File

class GradleVersionDetector : VersionDetector {

    override val id: String = "gradle"

    private val versionNameRegex = Regex("""\bversionName\s*(?:=)?\s*["']([^"']+)["']""")
    private val versionAssignRegex = Regex("""(?m)^\s*version\s*=\s*["']([^"']+)["']""")

    override fun detect(repoRoot: File, settings: DeploySettingsState): String? =
        candidateFiles(repoRoot, settings).firstNotNullOfOrNull { extractVersion(it) }

    override fun detectResult(repoRoot: File, settings: DeploySettingsState): VersionDetectionResult? =
        candidateFiles(repoRoot, settings).firstNotNullOfOrNull { file ->
            val (version, regex) = extractVersionAndRegex(file) ?: return@firstNotNullOfOrNull null
            VersionDetectionResult(version = version, sourceFile = file) { newVersion ->
                replaceGroupInFile(file, regex, newVersion)
            }
        }

    private fun candidateFiles(repoRoot: File, settings: DeploySettingsState): List<File> = buildList {
        settings.versionCustomPath.trim().takeIf { it.isNotBlank() }?.let { add(File(repoRoot, it)) }
        add(File(repoRoot, "build.gradle.kts"))
        add(File(repoRoot, "build.gradle"))
        add(File(repoRoot, "app/build.gradle.kts"))
        add(File(repoRoot, "app/build.gradle"))
    }.distinctBy { it.absolutePath }

    private fun extractVersion(file: File): String? = extractVersionAndRegex(file)?.first

    private fun extractVersionAndRegex(file: File): Pair<String, Regex>? {
        if (!file.isFile) return null
        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        versionNameRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
            ?.let { return Pair(it, versionNameRegex) }
        versionAssignRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
            ?.let { return Pair(it, versionAssignRegex) }
        return null
    }
}