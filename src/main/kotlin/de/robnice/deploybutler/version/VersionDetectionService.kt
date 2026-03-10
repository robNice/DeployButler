package de.robnice.deploybutler.version

import de.robnice.deploybutler.settings.DeploySettingsState
import de.robnice.deploybutler.version.detector.ComposerVersionDetector
import de.robnice.deploybutler.version.detector.CustomRegexVersionDetector
import de.robnice.deploybutler.version.detector.GradleVersionDetector
import de.robnice.deploybutler.version.detector.MavenVersionDetector
import de.robnice.deploybutler.version.detector.PackageJsonVersionDetector
import de.robnice.deploybutler.version.detector.VersionDetector
import java.io.File

class VersionDetectionService(
    private val settings: DeploySettingsState,
    private val detectors: List<VersionDetector> = listOf(
        CustomRegexVersionDetector(),
        GradleVersionDetector(),
        PackageJsonVersionDetector(),
        ComposerVersionDetector(),
        MavenVersionDetector()
    )
) {
    fun detect(repoRoot: File): String? {
        val preferred = settings.preferredVersionDetector.trim()

        val orderedDetectors = if (preferred.isBlank()) {
            detectors
        } else {
            detectors.sortedBy { if (it.id == preferred) 0 else 1 }
        }

        return orderedDetectors.firstNotNullOfOrNull { it.detect(repoRoot, settings) }
    }
}