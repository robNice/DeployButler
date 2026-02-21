package de.robnice.deploybutler.version

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.panel
import de.robnice.deploybutler.i18n.message
import javax.swing.JComponent

class ReleaseDialog(
    project: Project,
    private val currentVersion: Version,
    private val buildGradleVersionText: String?,
    private val tagPrefix: String
) : DialogWrapper(project) {

    private var selectedType: ReleaseType = ReleaseType.NONE

    init {
        title = message("dialog.release.title")
        init()
    }


    override fun createCenterPanel(): JComponent {
        val buildTagText = buildGradleVersionText?.let { "$tagPrefix$it" }
        val nextRevision = currentVersion.bump(ReleaseType.REVISION).toString()
        val nextFeature   = currentVersion.bump(ReleaseType.FEATURE).toString()
        val nextMajor     = currentVersion.bump(ReleaseType.MAJOR).toString()

        return panel {
            buttonsGroup {
                row {
                    radioButton(message("dialog.release.none"), ReleaseType.NONE)
                        .resizableColumn()
                    label("")
                        .align(AlignX.RIGHT)
                }

                if (buildGradleVersionText != null) {
                    row {
                        radioButton(message("dialog.release.fromBuild"), ReleaseType.FROM_BUILD_GRADLE)
                            .resizableColumn()
                        label(buildTagText ?: "")
                            .align(AlignX.RIGHT)
                    }
                }

                row {
                    radioButton(message("dialog.release.revision"), ReleaseType.REVISION)
                        .resizableColumn()
                    label("→ v$nextRevision").align(AlignX.RIGHT)
                }
                row {
                    radioButton(message("dialog.release.feature"), ReleaseType.FEATURE)
                        .resizableColumn()
                    label("→ v$nextFeature").align(AlignX.RIGHT)
                }
                row {
                    radioButton(message("dialog.release.major"), ReleaseType.MAJOR)
                        .resizableColumn()
                    label("→ v$nextMajor").align(AlignX.RIGHT)
                }
            }.bind({ selectedType }, { selectedType = it })
        }
    }


    fun getSelectedType(): ReleaseType = selectedType
}
