package de.robnice.deploybutler.version

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.panel
import de.robnice.deploybutler.i18n.message
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JSeparator
import javax.swing.JTextField

class ReleaseDialog(
    private val project: Project,
    private val currentVersion: Version,
    private val detectedVersionText: String?,
    private val tagPrefix: String,
    private val fixedTag: String?,
    private val detectionResult: VersionDetectionResult? = null,
    private val autoUpdateEnabled: Boolean = false
) : DialogWrapper(project) {

    private var selectedType: ReleaseType = ReleaseType.NONE
    private val customTagField = JTextField()
    private var fromProjectFileLabel: JLabel? = null

    init {
        title = message("dialog.release.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val nextRevision = currentVersion.bump(ReleaseType.REVISION).toString()
        val nextFeature = currentVersion.bump(ReleaseType.FEATURE).toString()
        val nextMajor = currentVersion.bump(ReleaseType.MAJOR).toString()
        val detectedTagText = detectedVersionText?.let { "$tagPrefix$it" }
        val configuredFixedTag = fixedTag?.trim().orEmpty().ifBlank { null }

        return panel {
            buttonsGroup {
                row {
                    radioButton(message("dialog.release.none"), ReleaseType.NONE)
                        .resizableColumn()
                    label("")
                        .align(AlignX.RIGHT)
                }

                row {
                    cell(JSeparator())
                        .resizableColumn()
                        .align(AlignX.FILL)
                    label("")
                }

                if (detectedVersionText != null) {
                    row {
                        radioButton(message("dialog.release.fromProjectFile"), ReleaseType.FROM_PROJECT_FILE)
                            .resizableColumn()
                        fromProjectFileLabel = label(detectedTagText ?: "").align(AlignX.RIGHT).component
                    }
                }

                row {
                    radioButton(message("dialog.release.revision"), ReleaseType.REVISION)
                        .resizableColumn()
                    label("-> $tagPrefix$nextRevision").align(AlignX.RIGHT)
                }
                row {
                    radioButton(message("dialog.release.feature"), ReleaseType.FEATURE)
                        .resizableColumn()
                    label("-> $tagPrefix$nextFeature").align(AlignX.RIGHT)
                }
                row {
                    radioButton(message("dialog.release.major"), ReleaseType.MAJOR)
                        .resizableColumn()
                    label("-> $tagPrefix$nextMajor").align(AlignX.RIGHT)
                }

                row {
                    cell(JSeparator())
                        .resizableColumn()
                        .align(AlignX.FILL)
                    label("")
                }

                if (configuredFixedTag != null) {
                    row {
                        radioButton(message("dialog.release.fixedTag"), ReleaseType.FIXED)
                            .resizableColumn()
                        label(configuredFixedTag).align(AlignX.RIGHT)
                    }
                }

                row {
                    radioButton(message("dialog.release.customTag"), ReleaseType.CUSTOM)
                        .resizableColumn()
                    cell(customTagField).align(AlignX.RIGHT)
                }
            }.bind({ selectedType }, { selectedType = it })

            if (autoUpdateEnabled && detectionResult != null) {
                row {
                    cell(JSeparator())
                        .resizableColumn()
                        .align(AlignX.FILL)
                    label("")
                }
                row {
                    button(message("dialog.release.updateVersionInFile")) {
                        showUpdateVersionInFileDialog()
                    }
                }
            }
        }
    }

    private fun showUpdateVersionInFileDialog() {
        val result = detectionResult ?: return
        val dialog = UpdateVersionInFileDialog(project, currentVersion, tagPrefix)
        if (!dialog.showAndGet()) return
        val newVersion = dialog.getNewVersion()
        val success = result.replaceVersion(newVersion)
        if (success) {
            fromProjectFileLabel?.text = "$tagPrefix$newVersion"
        } else {
            Messages.showErrorDialog(
                project,
                message("dialog.updateVersion.writeFailed"),
                message("dialog.updateVersion.title")
            )
        }
    }

    override fun doValidate(): ValidationInfo? {
        if (selectedType == ReleaseType.CUSTOM && customTagField.text.trim().isBlank()) {
            return ValidationInfo(message("dialog.release.customTag.required"), customTagField)
        }
        return null
    }

    fun getSelectedType(): ReleaseType = selectedType

    fun getCustomTag(): String = customTagField.text.trim()
}
