package de.robnice.deploybutler.version.detector

import de.robnice.deploybutler.settings.DeploySettingsState
import java.io.File

interface VersionDetector {
    val id: String
    fun detect(repoRoot: File, settings: DeploySettingsState): String?
}