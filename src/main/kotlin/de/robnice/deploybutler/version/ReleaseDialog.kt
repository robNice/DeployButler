package de.robnice.deploybutler.version

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import de.robnice.deploybutler.i18n.message
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.JTextField

class ReleaseDialog(
    private val project: Project,
    private val currentVersion: Version,
    private val detectedVersionText: String?,
    private val tagPrefix: String,
    private val fixedTag: String?,
    private val detectionResult: VersionDetectionResult? = null,
    private val autoUpdateEnabled: Boolean = false,
    private val tagExtensions: List<String> = emptyList()
) : DialogWrapper(project) {

    private var selectedType: ReleaseType = ReleaseType.NONE
    private val customTagField = JTextField()
    private val tagExtensionCombo = ComboBox(arrayOf("") + tagExtensions)
    private var fromProjectFileLabel: JLabel? = null
    private var formPanel: DialogPanel? = null
    private val annotationArea = JBTextArea(10, 30).apply {
        lineWrap = true
        wrapStyleWord = true
    }

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

        val formPanel = panel {
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
                    if (autoUpdateEnabled && detectionResult != null) {
                        row {
                            button(message("dialog.release.updateVersionInFile")) {
                                showUpdateVersionInFileDialog()
                            }
                        }
                        row {
                            cell(JSeparator())
                                .resizableColumn()
                                .align(AlignX.FILL)
                            label("")
                        }
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

            if (tagExtensions.isNotEmpty()) {
                row {
                    cell(JSeparator())
                        .resizableColumn()
                        .align(AlignX.FILL)
                    label("")
                }
                row {
                    label(message("dialog.release.tagExtension"))
                        .resizableColumn()
                    cell(tagExtensionCombo).align(AlignX.RIGHT)
                }
            }
        }

        this.formPanel = formPanel

        val annotationPanel = JPanel(BorderLayout(0, JBUI.scale(4))).apply {
            add(JLabel(message("dialog.release.annotation")), BorderLayout.NORTH)
            add(JBScrollPane(annotationArea).apply {
                preferredSize = Dimension(JBUI.scale(300), JBUI.scale(200))
            }, BorderLayout.CENTER)
        }

        return JPanel(BorderLayout(JBUI.scale(16), 0)).apply {
            add(formPanel, BorderLayout.WEST)
            add(annotationPanel, BorderLayout.CENTER)
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
        // The DSL panel is wrapped in a plain JPanel, so DialogWrapper does not apply it automatically.
        formPanel?.apply()
        if (selectedType == ReleaseType.CUSTOM && customTagField.text.trim().isBlank()) {
            return ValidationInfo(message("dialog.release.customTag.required"), customTagField)
        }
        return null
    }

    override fun doOKAction() {
        formPanel?.apply()
        super.doOKAction()
    }

    fun getSelectedType(): ReleaseType = selectedType

    fun getCustomTag(): String = customTagField.text.trim()

    fun getTagExtension(): String = (tagExtensionCombo.selectedItem as? String).orEmpty().trim()

    fun getAnnotation(): String = annotationArea.text.trim()
}
