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
import com.intellij.openapi.progress.ProcessCanceledException

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

    private fun repoForProjectBase(): GitRepository? = PendingAnnotation.repoForProject(project)

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

    private fun tagExistsLocally(repo: GitRepository, tag: String): Boolean {
        val result = runOrThrow(GitLineHandler(project, repo.root, GitCommand.TAG).apply {
            addParameters("-l", tag)
        })
        return result.output.any { it.trim() == tag }
    }

    private fun tagExistsOnRemote(repo: GitRepository, remote: String, tag: String): Boolean {
        val result = runOrThrow(GitLineHandler(project, repo.root, GitCommand.LS_REMOTE).apply {
            addParameters("--tags", remote, "refs/tags/$tag")
        })
        return result.output.any { it.contains("refs/tags/$tag") }
    }

    private fun deleteTagIfExists(repo: GitRepository, remote: String, tag: String) {
        if (tagExistsLocally(repo, tag)) {
            runOrThrow(GitLineHandler(project, repo.root, GitCommand.TAG).apply {
                addParameters("-d", tag)
            })
        }

        if (tagExistsOnRemote(repo, remote, tag)) {
            runOrThrow(GitLineHandler(project, repo.root, GitCommand.PUSH).apply {
                addParameters(remote, ":refs/tags/$tag")
            })
        }
    }

    private fun initialAnnotation(prefilled: String?): String =
        prefilled
            ?: settings.lastAnnotation.takeIf { settings.rememberAnnotation && it.isNotBlank() }
            ?: settings.defaultAnnotation

    private fun rememberSelection(type: ReleaseType, customTag: String, tagExtension: String, annotation: String) {
        if (settings.rememberTagOption) {
            settings.lastReleaseType = type.name
            settings.lastCustomTag = customTag
        }
        if (settings.rememberTagExtension) {
            settings.lastTagExtension = tagExtension
        }
        if (settings.rememberAnnotation) {
            settings.lastAnnotation = annotation
        }
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
            runOrThrow(GitLineHandler(project, repo.root, GitCommand.FETCH).apply { addParameters(remote) })
            runOrThrow(GitLineHandler(project, repo.root, GitCommand.FETCH).apply { addParameters("--tags") })

            val versionService = VersionService(project, repo, tagPrefix)
            val currentVersion = versionService.getLatestVersion()
            val versionDetectionService = VersionDetectionService(settings)
            val detectionResult = versionDetectionService.detectWithResult(File(repo.root.path))
            val detectedVersionText = detectionResult?.version

            val releaseTypeHolder = AtomicReference<ReleaseType?>(null)
            val selectedCustomTagHolder = AtomicReference("")
            val selectedTagExtensionHolder = AtomicReference("")
            val selectedAnnotationHolder = AtomicReference("")
            val okHolder = AtomicReference(false)

            val prefilledAnnotationFile = PendingAnnotation.file(project, repo)
            val prefilledAnnotation = PendingAnnotation.read(prefilledAnnotationFile)

            ApplicationManager.getApplication().invokeAndWait {
                val dialog = ReleaseDialog(
                    project,
                    currentVersion,
                    detectedVersionText,
                    tagPrefix,
                    settings.fixedTag,
                    detectionResult,
                    settings.autoUpdateVersion,
                    settings.tagExtensions.map { it.trim() }.filter { it.isNotBlank() },
                    initialType = if (settings.rememberTagOption) {
                        ReleaseType.entries.firstOrNull { it.name == settings.lastReleaseType }
                    } else null,
                    initialCustomTag = if (settings.rememberTagOption) settings.lastCustomTag else "",
                    initialTagExtension = if (settings.rememberTagExtension) settings.lastTagExtension else "",
                    initialAnnotation = initialAnnotation(prefilledAnnotation)
                )
                val ok = dialog.showAndGet()
                okHolder.set(ok)
                if (ok) {
                    releaseTypeHolder.set(dialog.getSelectedType())
                    selectedCustomTagHolder.set(dialog.getCustomTag())
                    selectedTagExtensionHolder.set(dialog.getTagExtension())
                    selectedAnnotationHolder.set(dialog.getAnnotation())
                    rememberSelection(
                        dialog.getSelectedType(),
                        dialog.getCustomTag(),
                        dialog.getTagExtension(),
                        dialog.getAnnotation()
                    )
                }
            }

            if (!okHolder.get()) {
                DeployNotifications.info(project, message("deploy.cancelled"))
                return
            }

            val releaseType = releaseTypeHolder.get() ?: ReleaseType.NONE

            val shouldReplaceExistingTag = releaseType == ReleaseType.FIXED || releaseType == ReleaseType.CUSTOM

            val baseTag: String? = when (releaseType) {
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
                ReleaseType.FIXED -> settings.fixedTag.trim().ifBlank {
                    DeployNotifications.error(project, message("deploy.fixedTagMissing"))
                    return
                }
                ReleaseType.CUSTOM -> selectedCustomTagHolder.get().trim().ifBlank {
                    DeployNotifications.error(project, message("deploy.customTagMissing"))
                    return
                }
                ReleaseType.REVISION, ReleaseType.FEATURE, ReleaseType.MAJOR ->
                    versionService.buildTag(currentVersion.bump(releaseType))
            }

            val tagExtension = selectedTagExtensionHolder.get()
            val newTag = baseTag?.let { if (tagExtension.isBlank()) it else "$it-$tagExtension" }
            val tagAnnotation = newTag?.let { selectedAnnotationHolder.get().replace(PendingAnnotation.TAG_PLACEHOLDER, it) }

            val deployChecks = settings.deployChecks
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (deployChecks.isNotEmpty()) {
                val proceedChecks = AtomicReference(false)
                ApplicationManager.getApplication().invokeAndWait {
                    proceedChecks.set(DeployChecksDialog(project, deployChecks).showAndGet())
                }
                if (!proceedChecks.get()) {
                    DeployNotifications.info(project, message("deploy.checks.cancelled"))
                    return
                }
            }

            val preview = DeployPreviewBuilder.build(
                sourceBranch = sourceBranch,
                targetBranch = targetBranch,
                tag = newTag ?: "(no tag)",
                settings = settings,
                annotation = tagAnnotation
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

            runOrThrow(GitLineHandler(project, repo.root, GitCommand.CHECKOUT).apply { addParameters(targetBranch) })

            if (settings.useRebase) {
                runOrThrow(GitLineHandler(project, repo.root, GitCommand.REBASE).apply { addParameters(sourceBranch) })
            } else {
                runOrThrow(GitLineHandler(project, repo.root, GitCommand.MERGE).apply { addParameters(sourceBranch) })
            }

            runOrThrow(GitLineHandler(project, repo.root, GitCommand.PUSH).apply { addParameters(remote, targetBranch) })

            if (newTag != null) {
                if (shouldReplaceExistingTag) {
                    deleteTagIfExists(repo, remote, newTag)
                }
                runOrThrow(GitLineHandler(project, repo.root, GitCommand.TAG).apply {
                    addParameters("-a", newTag, "-m", tagAnnotation.orEmpty())
                })
                runOrThrow(GitLineHandler(project, repo.root, GitCommand.PUSH).apply { addParameters(remote, newTag) })

                if (prefilledAnnotation != null) {
                    PendingAnnotation.delete(prefilledAnnotationFile)
                }
            }

            DeployNotifications.info(project, message("deploy.success", newTag ?: "(no tag)"))
        } catch (e: ProcessCanceledException) {
            throw e
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
