package de.robnice.deploybutler.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import de.robnice.deploybutler.git.DeployService
import de.robnice.deploybutler.i18n.message
import de.robnice.deploybutler.settings.DeploySettingsState

class DeployAction : AnAction() {


    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val settings = service<DeploySettingsState>()

        if (settings.confirmationsEnabled) {
            val result = Messages.showYesNoDialog(
                project,
                message("confirm.start"),
                message("action.deploy"),
                Messages.getQuestionIcon()
            )

            if (result != Messages.YES) {
                return
            }
        }

        DeployService(project, settings).run()
    }
}
