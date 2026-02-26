package de.robnice.deploybutler.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.panel
import de.robnice.deploybutler.i18n.message
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JTextField

class DeploySettingsConfigurable(
    private val project: Project
) : Configurable {

    private val settings = project.service<DeploySettingsState>()
    private val dryRunCheckbox = JCheckBox()
    private val branchField = JTextField()
    private val remoteField = JTextField()
    private val prefixField = JTextField()
    private val rebaseCheckbox = JCheckBox()
    private val confirmCheckbox = JCheckBox()

    override fun createComponent(): JComponent =
        panel {
            row { cell(dryRunCheckbox).label(message("settings.dryRun")) }
            row(message("settings.branch")) { cell(branchField).resizableColumn() }
            row(message("settings.remote")) { cell(remoteField).resizableColumn() }
            row(message("settings.prefix")) { cell(prefixField).resizableColumn() }
            row { cell(rebaseCheckbox).label(message("settings.rebase")) }
            row { cell(confirmCheckbox).label(message("settings.confirm")) }
        }

    override fun reset() {
        branchField.text = settings.targetBranch
        remoteField.text = settings.remoteName
        prefixField.text = settings.tagPrefix
        rebaseCheckbox.isSelected = settings.useRebase
        confirmCheckbox.isSelected = settings.confirmationsEnabled
        dryRunCheckbox.isSelected = settings.dryRunEnabled
    }

    override fun isModified(): Boolean =
        branchField.text.trim() != settings.targetBranch ||
                remoteField.text.trim() != settings.remoteName ||
                prefixField.text.trim() != settings.tagPrefix ||
                rebaseCheckbox.isSelected != settings.useRebase ||
                confirmCheckbox.isSelected != settings.confirmationsEnabled ||
                dryRunCheckbox.isSelected != settings.dryRunEnabled

    override fun apply() {
        settings.targetBranch = branchField.text.trim().ifBlank { "main" }
        settings.remoteName = remoteField.text.trim().ifBlank { "origin" }
        settings.tagPrefix = prefixField.text.trim().ifBlank { "v" }
        settings.useRebase = rebaseCheckbox.isSelected
        settings.confirmationsEnabled = confirmCheckbox.isSelected
        settings.dryRunEnabled = dryRunCheckbox.isSelected
    }

    override fun getDisplayName(): String = message("settings.title")
}