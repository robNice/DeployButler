package de.robnice.deploybutler.version

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.panel
import de.robnice.deploybutler.i18n.message
import javax.swing.JComponent

class UpdateVersionInFileDialog(
    project: Project,
    private val currentVersion: Version,
    private val tagPrefix: String
) : DialogWrapper(project) {

    private var selectedType: ReleaseType = ReleaseType.REVISION

    init {
        title = message("dialog.updateVersion.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val nextRevision = currentVersion.bump(ReleaseType.REVISION).toString()
        val nextFeature = currentVersion.bump(ReleaseType.FEATURE).toString()
        val nextMajor = currentVersion.bump(ReleaseType.MAJOR).toString()

        return panel {
            buttonsGroup {
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

    fun getNewVersion(): String = currentVersion.bump(selectedType).toString()
}
