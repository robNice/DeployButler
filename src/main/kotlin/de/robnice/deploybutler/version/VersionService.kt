package de.robnice.deploybutler.version

import com.intellij.openapi.project.Project
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository

class VersionService(
    private val project: Project,
    private val repo: GitRepository,
    private val tagPrefix: String
) {

    fun getLatestVersion(): Version {
        val handler = GitLineHandler(project, repo.root, GitCommand.TAG)
        val result = Git.getInstance().runCommand(handler)

        val tags = result.output
            .filter { it.startsWith(tagPrefix) }
            .map { it.removePrefix(tagPrefix) }
            .mapNotNull { runCatching { Version.parse(it) }.getOrNull() }

        if (tags.isEmpty()) {
            return Version(0, 0, 0)
        }

        return tags.maxOrNull()!!
    }

    fun buildTag(version: Version): String =
        "$tagPrefix$version"
}
