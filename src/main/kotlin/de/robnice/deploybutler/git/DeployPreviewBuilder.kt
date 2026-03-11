package de.robnice.deploybutler.git

import de.robnice.deploybutler.i18n.message
import de.robnice.deploybutler.settings.DeploySettingsState

object DeployPreviewBuilder {

    fun build(
        sourceBranch: String,
        targetBranch: String,
        tag: String,
        settings: DeploySettingsState
    ): String {
        val operationLabel = if (settings.useRebase) {
            message("preview.strategy.rebase")
        } else {
            message("preview.strategy.merge")
        }

        val deployChecks = settings.deployChecks
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return buildString {
            appendLine(message("preview.header"))
            appendLine()
            appendLine(message("preview.sourceBranch", sourceBranch))
            appendLine(message("preview.targetBranch", targetBranch))
            appendLine(message("preview.remote", settings.remoteName))
            appendLine(message("preview.strategy", operationLabel))
            appendLine(message("preview.tagToCreate", tag))

            if (deployChecks.isNotEmpty()) {
                appendLine()
                appendLine(message("preview.deployChecks"))
                deployChecks.forEach { check ->
                    appendLine(message("preview.deployCheckItem", check))
                }
            }

            appendLine()
            appendLine(message("preview.plannedSteps"))
            appendLine(message("preview.step.fetch", settings.remoteName))
            appendLine(message("preview.step.fetchTags"))
            appendLine(message("preview.step.checkout", targetBranch))
            appendLine(message("preview.step.integrate", operationLabel, sourceBranch, targetBranch))
            appendLine(message("preview.step.pushBranch", settings.remoteName, targetBranch))
            appendLine(message("preview.step.createTag", tag))
            appendLine(message("preview.step.pushTag", settings.remoteName, tag))
        }.trimEnd()
    }
}