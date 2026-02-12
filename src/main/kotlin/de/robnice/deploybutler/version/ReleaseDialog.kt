package de.robnice.deploybutler.version

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bind
import de.robnice.deploybutler.i18n.message
import javax.swing.*

class ReleaseDialog(
    project: Project,
    private val currentVersion: Version
) : DialogWrapper(project) {

    private var selectedType: ReleaseType = ReleaseType.REVISION

    init {
        title = message("dialog.release.title")
        init()
    }

    override fun createCenterPanel(): JComponent {

        return panel {
            buttonsGroup {
                row {
                    radioButton(
                        message("dialog.release.revision",
                            "v${currentVersion.bump(ReleaseType.REVISION)}"),
                        ReleaseType.REVISION
                    )

                }
                row {
                    radioButton(
                        message("dialog.release.feature",
                            "v${currentVersion.bump(ReleaseType.FEATURE)}"),
                        ReleaseType.FEATURE
                    )

                }
                row {
                    radioButton(
                        message("dialog.release.major",
                            "v${currentVersion.bump(ReleaseType.MAJOR)}"),
                        ReleaseType.MAJOR
                    )

                }
            }.bind(
                { selectedType },
                { selectedType = it }
            )
        }
    }

    fun getSelectedType(): ReleaseType = selectedType
}
