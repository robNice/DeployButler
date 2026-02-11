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

        return """
            DeployButler – Dry Run

            Source branch : $sourceBranch
            Target branch : $targetBranch
            Strategy      : $op
            Tag to create : $tag

            Planned steps:
            - checkout $targetBranch
            - $op $sourceBranch → $targetBranch
            - push $targetBranch
            - create tag $tag
            - push tag $tag

            ⚠️ No changes will be made.
        """.trimIndent()
    }
}
