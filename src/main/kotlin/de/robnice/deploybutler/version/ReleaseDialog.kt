package de.robnice.deploybutler.version

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import de.robnice.deploybutler.i18n.message
import javax.swing.*

class ReleaseDialog(
    project: Project,
    currentVersion: Version
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
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(revisionButton)
            add(featureButton)
            add(majorButton)
        }
    }

    fun getSelectedType(): ReleaseType =
        when {
            revisionButton.isSelected -> ReleaseType.REVISION
            featureButton.isSelected -> ReleaseType.FEATURE
            else -> ReleaseType.MAJOR
        }
}
