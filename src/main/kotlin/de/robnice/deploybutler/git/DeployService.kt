package de.robnice.deploybutler.git

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import de.robnice.deploybutler.i18n.message
import de.robnice.deploybutler.notify.DeployNotifications
import de.robnice.deploybutler.settings.DeploySettingsState
import de.robnice.deploybutler.version.ReleaseDialog
import de.robnice.deploybutler.version.ReleaseType
import de.robnice.deploybutler.version.Version
import de.robnice.deploybutler.version.VersionDetectionService
import de.robnice.deploybutler.version.VersionService
import git4idea.GitUtil
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import java.io.File
import java.util.concurrent.atomic.AtomicReference

class DeployService(
    private val project: Project,
    private val settings: DeploySettingsState
) {

    private fun runOrThrow(handler: GitLineHandler): GitCommandResult {
        val result = Git.getInstance().runCommand(handler)
        if (!result.success()) {
            val err = buildString {
                if (result.errorOutput.isNotEmpty()) append(result.errorOutput.joinToString("\n"))
                if (result.output.isNotEmpty()) {
                    if (isNotBlank()) append("\n")
                    append(result.output.joinToString("\n"))
                }
            }.ifBlank { "Unknown git error" }
            throw RuntimeException(err)
        }
        return result
    }

    private fun repoForProjectBase(): GitRepository? {
        val mgr = GitUtil.getRepositoryManager(project)
        val base = project.basePath ?: return mgr.repositories.firstOrNull()
        val match = mgr.repositories.firstOrNull { base.startsWith(it.root.path) }
        return match ?: mgr.repositories.firstOrNull()
    }

    private fun hasUncommittedChanges(repo: GitRepository): Boolean {
        val handler = GitLineHandler(project, repo.root, GitCommand.STATUS)
        handler.addParameters("--porcelain")
        val result = Git.getInstance().runCommand(handler)
        return result.output.isNotEmpty()
    }

    private fun parseStrictSemver(raw: String): Version? {
        val m = Regex("^(\\d+)\\.(\\d+)\\.(\\d+)$").matchEntire(raw.trim()) ?: return null
        return Version(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
    }

    fun run() {
        val repo = repoForProjectBase() ?: run {
            DeployNotifications.error(project, message("deploy.noRepo"))
            return
        }

        if (hasUncommittedChanges(repo)) {
            DeployNotifications.warning(project, message("deploy.dirty"))
            return
        }

        val startBranch = repo.currentBranch?.name ?: run {
            DeployNotifications.error(project, message("deploy.noBranch"))
            return
        }

        val sourceBranch = startBranch
        val targetBranch = settings.targetBranch.trim().ifBlank { "main" }
        val remote = settings.remoteName.trim().ifBlank { "origin" }
        val tagPrefix = settings.tagPrefix.trim().ifBlank { "v" }

        if (sourceBranch == targetBranch) {
            DeployNotifications.warning(project, message("deploy.onTarget", targetBranch))
            return
        }

        try {
            // PLAN (no checkout/merge/push yet)
            runOrThrow(GitLineHandler(project, repo.root, GitCommand.FETCH).apply { addParameters(remote) })
            runOrThrow(GitLineHandler(project, repo.root, GitCommand.FETCH).apply { addParameters("--tags") })

            val versionService = VersionService(project, repo, tagPrefix)
            val currentVersion = versionService.getLatestVersion()
            val versionDetectionService = VersionDetectionService(settings)
            val detectedVersionText = versionDetectionService.detect(File(repo.root.path))

            val releaseTypeHolder = AtomicReference<ReleaseType?>(null)
            val okHolder = AtomicReference(false)

            ApplicationManager.getApplication().invokeAndWait {
                val dialog = ReleaseDialog(project, currentVersion, detectedVersionText, settings.tagPrefix)
                val ok = dialog.showAndGet()
                okHolder.set(ok)
                if (ok) releaseTypeHolder.set(dialog.getSelectedType())
            }

            if (!okHolder.get()) {
                DeployNotifications.info(project, message("deploy.cancelled"))
                return
            }

            val releaseType = releaseTypeHolder.get() ?: ReleaseType.NONE

            val newTag: String? = when (releaseType) {
                ReleaseType.NONE -> null

                ReleaseType.FROM_PROJECT_FILE -> {
                    val v = detectedVersionText ?: run {
                        DeployNotifications.error(project, message("deploy.buildVersionMissing"))
                        return
                    }
                    val semver = parseStrictSemver(v) ?: run {
                        DeployNotifications.error(project, message("deploy.buildVersionMissing"))
                        return
                    }
                    versionService.buildTag(semver)
                }

                ReleaseType.REVISION, ReleaseType.FEATURE, ReleaseType.MAJOR ->
                    versionService.buildTag(currentVersion.bump(releaseType))
            }

            val preview = DeployPreviewBuilder.build(
                sourceBranch = sourceBranch,
                targetBranch = targetBranch,
                tag = newTag ?: "(no tag)",
                settings = settings
            )

            if (settings.dryRunEnabled) {
                ApplicationManager.getApplication().invokeAndWait {
                    DeployPreviewDialog(project, preview, proceedEnabled = false).show()
                }
                DeployNotifications.info(project, message("deploy.cancelled"))
                return
            }

            if (settings.confirmationsEnabled) {
                val proceed = AtomicReference(false)
                ApplicationManager.getApplication().invokeAndWait {
                    proceed.set(DeployPreviewDialog(project, preview, proceedEnabled = true).showAndGet())
                }
                if (!proceed.get()) {
                    DeployNotifications.info(project, message("deploy.preview.cancelled"))
                    return
                }
            }

            // EXECUTE
            runOrThrow(GitLineHandler(project, repo.root, GitCommand.CHECKOUT).apply { addParameters(targetBranch) })

            if (settings.useRebase) {
                runOrThrow(GitLineHandler(project, repo.root, GitCommand.REBASE).apply { addParameters(sourceBranch) })
            } else {
                runOrThrow(GitLineHandler(project, repo.root, GitCommand.MERGE).apply { addParameters(sourceBranch) })
            }

            runOrThrow(GitLineHandler(project, repo.root, GitCommand.PUSH).apply { addParameters(remote, targetBranch) })

            if (newTag != null) {
                runOrThrow(GitLineHandler(project, repo.root, GitCommand.TAG).apply {
                    addParameters("-a", newTag, "-m", "Deploy $newTag")
                })
                runOrThrow(GitLineHandler(project, repo.root, GitCommand.PUSH).apply { addParameters(remote, newTag) })
            }

            DeployNotifications.info(project, message("deploy.success", newTag ?: "(no tag)"))
        } catch (e: Exception) {
            DeployNotifications.error(project, message("deploy.gitFailed", e.message ?: ""))
        } finally {
            try {
                repo.update()

                val result = Git.getInstance().runCommand(
                    GitLineHandler(project, repo.root, GitCommand.CHECKOUT).apply {
                        addParameters(startBranch)
                    }
                )

                repo.update()

                if (!result.success()) {
                    val err = buildString {
                        if (result.errorOutput.isNotEmpty()) append(result.errorOutput.joinToString("\n"))
                        if (result.output.isNotEmpty()) {
                            if (isNotBlank()) append("\n")
                            append(result.output.joinToString("\n"))
                        }
                    }.ifBlank { message("deploy.restoreBranch.unknownError") }

                    DeployNotifications.error(
                        project,
                        message("deploy.restoreBranch.failed", startBranch, err)
                    )
                }
            } catch (e: Exception) {
                DeployNotifications.error(
                    project,
                    message("deploy.restoreBranch.failed", startBranch, e.message ?: message("deploy.restoreBranch.unknownError"))
                )
            }
        }
    }
}
