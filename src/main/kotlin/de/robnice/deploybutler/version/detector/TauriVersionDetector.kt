package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
import de.robnice.deploybutler.version.VersionDetectionResult
import java.io.File

class TauriVersionDetector : VersionDetector {

    override val id: String = "tauri"

    private val jsonVersionRegex = Regex(""""version"\s*:\s*"([^"]+)"""")
    private val tomlVersionRegex = Regex("""(?m)^\s*version\s*=\s*"([^"]+)"""")
    private val packageJsonVersionRegex = Regex(""""version"\s*:\s*"([^"]+)"""")
    private val packageHeaderRegex = Regex("""^\s*\[package]\s*(?:#.*)?$""")
    private val sectionHeaderRegex = Regex("""^\s*\[[^\]]+]\s*(?:#.*)?$""")

    private val configFileNames = listOf(
        "tauri.conf.json",
        "tauri.conf.json5",
        "Tauri.toml",
        "tauri.linux.conf.json",
        "tauri.windows.conf.json",
        "tauri.macos.conf.json",
        "tauri.android.conf.json",
        "tauri.ios.conf.json",
        "Tauri.linux.toml",
        "Tauri.windows.toml",
        "Tauri.macos.toml",
        "Tauri.android.toml",
        "Tauri.ios.toml"
    )

    override fun detect(repoRoot: File, settings: DeploySettingsState): String? =
        detectResult(repoRoot, settings)?.version

    override fun detectResult(repoRoot: File, settings: DeploySettingsState): VersionDetectionResult? =
        candidateFiles(repoRoot).firstNotNullOfOrNull { file ->
            when {
                file.name.equals("Cargo.toml", ignoreCase = true) -> detectCargoToml(file)
                file.extension.equals("toml", ignoreCase = true) -> detectTomlConfig(file)
                else -> detectJsonConfig(file)
            }
        }

    private fun candidateFiles(repoRoot: File): List<File> {
        val configFiles = configFileNames.flatMap { name ->
            listOf(File(repoRoot, "src-tauri/$name"), File(repoRoot, name))
        }.filter { it.isFile }

        if (configFiles.isEmpty()) return emptyList()

        return buildList {
            addAll(configFiles)
            add(File(repoRoot, "src-tauri/Cargo.toml"))
            add(File(repoRoot, "Cargo.toml"))
        }.filter { it.isFile }
            .distinctBy { it.absolutePath }
    }

    private fun detectJsonConfig(file: File): VersionDetectionResult? =
        detectConfigVersion(file, jsonVersionRegex)

    private fun detectTomlConfig(file: File): VersionDetectionResult? =
        detectConfigVersion(file, tomlVersionRegex)

    private fun detectConfigVersion(file: File, regex: Regex): VersionDetectionResult? {
        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        val match = regex.find(text) ?: return null
        val group = match.groups[1] ?: return null
        val version = group.value.trim().takeIf { it.isNotBlank() } ?: return null
        if (version.endsWith("package.json", ignoreCase = true)) {
            return detectPackageJson(File(file.parentFile, version))
        }
        return VersionDetectionResult(version = version, sourceFile = file) { newVersion ->
            replaceGroupInFile(file, regex, newVersion)
        }
    }

    private fun detectPackageJson(file: File): VersionDetectionResult? {
        if (!file.isFile) return null
        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        val version = packageJsonVersionRegex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
            ?: return null
        return VersionDetectionResult(version = version, sourceFile = file) { newVersion ->
            replaceGroupInFile(file, packageJsonVersionRegex, newVersion)
        }
    }

    private fun detectCargoToml(file: File): VersionDetectionResult? {
        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        var offset = 0
        var inPackageSection = false

        for (rawLine in text.lineSequence()) {
            val line = rawLine.removeSuffix("\r")
            val lineStart = offset
            offset += rawLine.length + 1

            when {
                packageHeaderRegex.matches(line) -> inPackageSection = true
                inPackageSection && sectionHeaderRegex.matches(line) -> return null
                inPackageSection -> {
                    val match = tomlVersionRegex.find(line) ?: continue
                    val group = match.groups[1] ?: continue
                    val version = group.value.trim().takeIf { it.isNotBlank() } ?: continue
                    return VersionDetectionResult(version = version, sourceFile = file) { newVersion ->
                        replaceRangeInFile(file, lineStart + group.range.first, lineStart + group.range.last + 1, newVersion)
                    }
                }
            }
        }

        return null
    }

    private fun replaceRangeInFile(file: File, start: Int, end: Int, replacement: String): Boolean {
        val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return false
        if (start !in 0..text.length || end !in start..text.length) return false
        val newText = text.substring(0, start) + replacement + text.substring(end)
        return runCatching { file.writeText(newText, Charsets.UTF_8) }.isSuccess
    }
}
