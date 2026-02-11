package de.robnice.deploybutler.git

import com.intellij.openapi.project.Project
import de.robnice.deploybutler.notify.DeployNotifications
import de.robnice.deploybutler.settings.DeploySettingsState
import de.robnice.deploybutler.version.*
import de.robnice.deploybutler.i18n.message
import git4idea.GitUtil
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository

class DeployService(
    private val project: Project,
    private val settings: DeploySettingsState
) {

    private fun hasUncommittedChanges(project: Project, repo: GitRepository): Boolean {
        val handler = GitLineHandler(project, repo.root, GitCommand.STATUS)
        handler.addParameters("--porcelain")
        val result = Git.getInstance().runCommand(handler)
        return result.output.isNotEmpty()
    }

    fun run() {

        val repo = GitUtil.getRepositoryManager(project)
            .repositories
            .firstOrNull()
            ?: run {
                DeployNotifications.error(project, message("deploy.noRepo"))
                return
            }

        if (hasUncommittedChanges(project, repo)) {
            DeployNotifications.warning(project, message("deploy.dirty"))
            return
        }

        val startBranch = repo.currentBranch?.name
            ?: run {
                DeployNotifications.error(project, message("deploy.noBranch"))
                return
            }

        val sourceBranch = startBranch
        val targetBranch = settings.targetBranch

        if (sourceBranch == targetBranch) {
            DeployNotifications.warning(project, message("deploy.onTarget", targetBranch))
            return
        }

        val git = Git.getInstance()

        try {
            // checkout target branch
            git.checkout(
                repo,
                targetBranch,
                null,
                false,
                false
            )

            // merge or rebase source -> target (see settings)
            if (settings.useRebase) {
                val rebaseHandler = GitLineHandler(project, repo.root, GitCommand.REBASE)
                rebaseHandler.addParameters(sourceBranch)
                git.runCommand(rebaseHandler)
            } else {
                git.merge(repo, sourceBranch, null)
            }

            // push target branch
            val pushHandler = GitLineHandler(project, repo.root, GitCommand.PUSH)
            pushHandler.addParameters("origin", targetBranch)
            git.runCommand(pushHandler)

            // versioning
            val versionService = VersionService(project, repo, settings.tagPrefix)

            val currentVersion = versionService.getLatestVersion()

            val dialog = ReleaseDialog(project, currentVersion)
            if (!dialog.showAndGet()) {
                DeployNotifications.info(project, message("deploy.cancelled"))
                return
            }

            val newVersion = currentVersion.bump(dialog.getSelectedType())
            val newTag = versionService.buildTag(newVersion)

            val preview = DeployPreviewBuilder.build(
                sourceBranch = sourceBranch,
                targetBranch = targetBranch,
                tag = newTag,
                settings = settings
            )

            if (settings.dryRunEnabled) {
                val dialog = DeployPreviewDialog(project, preview)
                dialog.show()
                return
            }

            // create and push tag
            try {
                val tagHandler = GitLineHandler(project, repo.root, GitCommand.TAG)
                tagHandler.addParameters("-a", newTag, "-m", "Deploy $newTag")
                git.runCommand(tagHandler)
                val pushTagHandler = GitLineHandler(project, repo.root, GitCommand.PUSH)
                pushTagHandler.addParameters("origin", newTag)
                git.runCommand(pushTagHandler)
            } catch (e: Exception) {
                DeployNotifications.error(
                    project,
                    message("deploy.tagFailed", newTag, e.message ?: "")
                )
                return
            }
            DeployNotifications.info(project, message("deploy.success", newTag))
        } catch (e: Exception) {
            DeployNotifications.error(project, message("deploy.gitFailed", e.message ?: ""))
        } finally {
            // return to the start branch
            if (repo.currentBranch?.name != startBranch) {
                val backHandler = GitLineHandler(project, repo.root, GitCommand.CHECKOUT)
                backHandler.addParameters(startBranch)
                git.runCommand(backHandler)
            }
        }
    }
}
