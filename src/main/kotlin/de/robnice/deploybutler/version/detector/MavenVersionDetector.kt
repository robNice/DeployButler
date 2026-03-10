package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
import java.io.File

class MavenVersionDetector : VersionDetector {

    override val id: String = "maven"

    private val versionRegex = Regex("""<version>\s*([^<\s]+)\s*</version>""")

    override fun detect(repoRoot: File, settings: DeploySettingsState): String? {
        val file = File(repoRoot, "pom.xml")
        if (!file.isFile) return null

        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        return versionRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }
}