package de.robnice.deploybutler.git

import de.robnice.deploybutler.settings.DeploySettingsState

object DeployPreviewBuilder {

    fun build(
        sourceBranch: String,
        targetBranch: String,
        tag: String,
        settings: DeploySettingsState
    ): String {
        val op = if (settings.useRebase) "Rebase" else "Merge"

        return buildString {
            appendLine("DeployButler - Preview")
            appendLine()
            appendLine("Source branch : $sourceBranch")
            appendLine("Target branch : $targetBranch")
            appendLine("Remote        : ${settings.remoteName}")
            appendLine("Strategy      : $op")
            appendLine("Tag to create : $tag")
            appendLine()
            appendLine("Planned steps:")
            appendLine("- fetch ${settings.remoteName}")
            appendLine("- fetch --tags")
            appendLine("- checkout $targetBranch")
            appendLine("- $op $sourceBranch -> $targetBranch")
            appendLine("- push ${settings.remoteName} $targetBranch")
            appendLine("- create tag $tag")
            appendLine("- push ${settings.remoteName} $tag")
        }.trimEnd()
    }
}
