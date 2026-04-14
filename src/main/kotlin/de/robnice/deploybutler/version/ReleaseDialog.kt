package de.robnice.deploybutler.version

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.panel
import de.robnice.deploybutler.i18n.message
import javax.swing.JComponent
import javax.swing.JTextField

class ReleaseDialog(
    project: Project,
    private val currentVersion: Version,
    private val detectedVersionText: String?,
    private val tagPrefix: String,
    private val fixedTag: String?
) : DialogWrapper(project) {

    private var selectedType: ReleaseType = ReleaseType.NONE
    private val customTagField = JTextField()

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

                if (detectedVersionText != null) {
                    row {
                        radioButton(message("dialog.release.fromProjectFile"), ReleaseType.FROM_PROJECT_FILE)
                            .resizableColumn()
                        label(detectedTagText ?: "").align(AlignX.RIGHT)
                    }
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
                    cell(customTagField).resizableColumn()
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
            }.bind({ selectedType }, { selectedType = it })
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
