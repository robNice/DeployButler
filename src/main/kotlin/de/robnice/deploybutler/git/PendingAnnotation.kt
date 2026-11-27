package de.robnice.deploybutler.git

import com.intellij.openapi.project.Project
import git4idea.GitUtil
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import java.io.File

/**
 * Annotation that is pre-filled into the release dialog of the next deploy.
 * It is stored in a file inside the Git directory, so scripts, CLI tools and AI agents can set it.
 */
object PendingAnnotation {
    const val FILE_NAME = "DEPLOYBUTLER_ANNOTATION"
    const val TAG_PLACEHOLDER = "{tag}"

    fun repoForProject(project: Project): GitRepository? {
        val mgr = GitUtil.getRepositoryManager(project)
        val base = project.basePath ?: return mgr.repositories.firstOrNull()
        val match = mgr.repositories.firstOrNull { base.startsWith(it.root.path) }
        return match ?: mgr.repositories.firstOrNull()
    }

    fun file(project: Project, repo: GitRepository): File? = runCatching {
        val result = Git.getInstance().runCommand(GitLineHandler(project, repo.root, GitCommand.REV_PARSE).apply {
            addParameters("--git-dir")
        })
        if (!result.success()) return null
        val gitDir = result.output.firstOrNull()?.trim()?.ifBlank { null } ?: return null
        val dir = File(gitDir).let { if (it.isAbsolute) it else File(repo.root.path, gitDir) }
        File(dir, FILE_NAME)
    }.getOrNull()

    fun read(file: File?): String? = runCatching {
        file?.takeIf { it.isFile }?.readText(Charsets.UTF_8)?.removePrefix("﻿")?.trim()?.ifBlank { null }
    }.getOrNull()

    fun write(file: File, text: String) {
        file.writeText(text.trim() + "\n", Charsets.UTF_8)
    }

    fun delete(file: File?) {
        runCatching { file?.delete() }
    }
}
