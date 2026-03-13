package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
import java.io.File

class PackageJsonVersionDetector : VersionDetector {

    override val id: String = "package-json"

    private val versionRegex = Regex(""""version"\s*:\s*"([^"]+)"""")
    private val directSearchPaths = listOf(
        "package.json",
        "app/package.json",
        "frontend/package.json",
        "client/package.json",
        "web/package.json",
        "ui/package.json",
        "www/package.json"
    )
    private val monorepoDirectories = listOf("apps", "packages", "services")

    override fun detect(repoRoot: File, settings: DeploySettingsState): String? {
        val packageJsonFiles = buildList {
            directSearchPaths
                .map { File(repoRoot, it) }
                .filterTo(this) { it.isFile }

            monorepoDirectories.forEach { directoryName ->
                val directory = File(repoRoot, directoryName)
                directory.listFiles()
                    ?.sortedBy { it.name.lowercase() }
                    ?.forEach { child ->
                        val file = File(child, "package.json")
                        if (file.isFile) {
                            add(file)
                        }
                    }
            }
        }

        return packageJsonFiles.firstNotNullOfOrNull { file ->
            val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return@firstNotNullOfOrNull null
            versionRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
        }
    }
}
