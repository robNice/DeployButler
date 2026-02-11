package de.robnice.deploybutler.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.components.service
import com.intellij.ui.dsl.builder.panel
import de.robnice.deploybutler.i18n.message
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.JCheckBox

class DeploySettingsConfigurable : Configurable {

    private val settings = service<DeploySettingsState>()
    private val dryRunCheckbox = JCheckBox()
    private val branchField = JTextField()
    private val prefixField = JTextField()
    private val rebaseCheckbox = JCheckBox()
    private val confirmCheckbox = JCheckBox()

    override fun createComponent(): JComponent {
        branchField.text = settings.targetBranch
        prefixField.text = settings.tagPrefix
        rebaseCheckbox.isSelected = settings.useRebase
        confirmCheckbox.isSelected = settings.confirmationsEnabled
        dryRunCheckbox.isSelected = settings.dryRunEnabled

        return panel {
            row {
                cell(dryRunCheckbox)
                    .label(message("settings.dryRun"))
            }
            row(message("settings.branch")) {
                cell(branchField)
                    .resizableColumn()
            }
            row(message("settings.prefix")) {
                cell(prefixField)
                    .resizableColumn()
            }
            row {
                cell(rebaseCheckbox)
                    .label(message("settings.rebase"))
            }
            row {
                cell(confirmCheckbox)
                    .label(message("settings.confirm"))
            }
        }
    }


    override fun isModified(): Boolean =
        branchField.text != settings.targetBranch ||
        prefixField.text != settings.tagPrefix ||
        rebaseCheckbox.isSelected != settings.useRebase ||
        confirmCheckbox.isSelected != settings.confirmationsEnabled ||
        dryRunCheckbox.isSelected != settings.dryRunEnabled

    override fun apply() {
        settings.targetBranch = branchField.text.trim()
        settings.tagPrefix = prefixField.text.trim()
        settings.useRebase = rebaseCheckbox.isSelected
        settings.confirmationsEnabled = confirmCheckbox.isSelected
        settings.dryRunEnabled = dryRunCheckbox.isSelected
    }

    override fun getDisplayName(): String =
        message("settings.title")
}
