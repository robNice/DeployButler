package de.robnice.deploybutler.version

import java.io.File

data class VersionDetectionResult(
    val version: String,
    val sourceFile: File,
    val replaceVersion: (newVersion: String) -> Boolean
)
