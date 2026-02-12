package de.robnice.deploybutler.version

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.AlignX
import de.robnice.deploybutler.i18n.message
import javax.swing.*

class ReleaseDialog(
    project: Project,
    private val currentVersion: Version
) : DialogWrapper(project) {

    private val revisionButton = JRadioButton(
        message("dialog.release.revision", "v${currentVersion.bump(ReleaseType.REVISION)}")    )

    private val featureButton = JRadioButton(
        message("dialog.release.feature", "v${currentVersion.bump(ReleaseType.FEATURE)}")    )

    private val majorButton = JRadioButton(
        message("dialog.release.major", "v${currentVersion.bump(ReleaseType.MAJOR)}")    )

    init {
        ButtonGroup().apply {
            add(revisionButton)
            add(featureButton)
            add(majorButton)
        }
        title = message("dialog.release.title")
        revisionButton.isSelected = true
        init()
    }

    override fun createCenterPanel(): JComponent {

        return panel {
            row {
                cell(revisionButton)
                label("v${currentVersion.bump(ReleaseType.REVISION)}")
                    .align(AlignX.RIGHT)
            }
            row {
                cell(featureButton)
                label("v${currentVersion.bump(ReleaseType.FEATURE)}")
                    .align(AlignX.RIGHT)
            }
            row {
                cell(majorButton)
                label("v${currentVersion.bump(ReleaseType.MAJOR)}")
                    .align(AlignX.RIGHT)
            }
        }
    }

    fun getSelectedType(): ReleaseType =
        when {
            revisionButton.isSelected -> ReleaseType.REVISION
            featureButton.isSelected -> ReleaseType.FEATURE
            else -> ReleaseType.MAJOR
        }
}
