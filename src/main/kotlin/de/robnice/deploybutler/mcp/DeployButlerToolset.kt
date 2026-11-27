package de.robnice.deploybutler.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.mcpFail
import com.intellij.mcpserver.project
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import de.robnice.deploybutler.git.PendingAnnotation
import de.robnice.deploybutler.settings.DeploySettingsState
import de.robnice.deploybutler.version.ReleaseType
import de.robnice.deploybutler.version.VersionDetectionService
import de.robnice.deploybutler.version.VersionService
import git4idea.repo.GitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import java.io.File

/**
 * MCP tools offered through the built-in MCP server of the IDE (optional dependency on com.intellij.mcpServer).
 */
class DeployButlerToolset : McpToolset {

    @McpTool(name = "deploybutler_set_annotation")
    @McpDescription(
        description = """
        Sets the Git tag annotation that DeployButler pre-fills in its release dialog for the next deploy of this project.
        Use it after writing release notes or a changelog entry, so the user only has to confirm the deploy in the IDE.
        The placeholder {tag} is replaced with the final tag name (e.g. "Release {tag}").
        An empty text clears the pending annotation. The annotation is consumed (deleted) once the tag has been created.
        This tool does not start a deploy.
        """
    )
    suspend fun deploybutler_set_annotation(
        @McpDescription(description = "Annotation text (plain text, multiple lines allowed). Empty text clears it.")
        text: String
    ): String {
        val project = currentCoroutineContext().project
        return withContext(Dispatchers.IO) {
            val file = annotationFile(project)
            if (text.isBlank()) {
                PendingAnnotation.delete(file)
                "Pending DeployButler annotation cleared."
            } else {
                PendingAnnotation.write(file, text)
                "Annotation saved to ${file.path}. It will be pre-filled in the DeployButler release dialog of the next deploy."
            }
        }
    }

    @McpTool(name = "deploybutler_get_info")
    @McpDescription(
        description = """
        Returns DeployButler information for this project: current and target branch, tag prefix, latest version tag,
        the next possible tags (revision, feature, major), the version from the project file,
        configured tag extensions and default annotation, and the currently pending annotation.
        Useful before writing release notes or calling deploybutler_set_annotation.
        """
    )
    suspend fun deploybutler_get_info(): String {
        val project = currentCoroutineContext().project
        return withContext(Dispatchers.IO) {
            val settings = project.service<DeploySettingsState>()
            val repo = repository(project)
            val tagPrefix = settings.tagPrefix.trim().ifBlank { "v" }
            val versionService = VersionService(project, repo, tagPrefix)
            val current = versionService.getLatestVersion()
            val detected = VersionDetectionService(settings).detect(File(repo.root.path))
            val file = PendingAnnotation.file(project, repo)
            val pending = PendingAnnotation.read(file)

            buildString {
                appendLine("repository: ${repo.root.path}")
                appendLine("current branch: ${repo.currentBranch?.name ?: "(detached HEAD)"}")
                appendLine("target branch: ${settings.targetBranch.trim().ifBlank { "main" }}")
                appendLine("remote: ${settings.remoteName.trim().ifBlank { "origin" }}")
                appendLine("tag prefix: $tagPrefix")
                appendLine("latest version tag (local tags): ${versionService.buildTag(current)}")
                appendLine("next revision tag: ${versionService.buildTag(current.bump(ReleaseType.REVISION))}")
                appendLine("next feature tag: ${versionService.buildTag(current.bump(ReleaseType.FEATURE))}")
                appendLine("next major tag: ${versionService.buildTag(current.bump(ReleaseType.MAJOR))}")
                appendLine("project file version: ${detected?.let { "$it (tag $tagPrefix$it)" } ?: "(none detected)"}")
                appendLine("fixed tag: ${settings.fixedTag.ifBlank { "(none)" }}")
                appendLine("tag extensions: ${settings.tagExtensions.filter { it.isNotBlank() }.joinToString().ifBlank { "(none)" }}")
                appendLine("default annotation: ${settings.defaultAnnotation.ifBlank { "(none)" }}")
                appendLine("pending annotation file: ${file?.path ?: "(unknown)"}")
                appendLine("pending annotation: ${pending ?: "(none)"}")
                appendLine("placeholder: ${PendingAnnotation.TAG_PLACEHOLDER} is replaced with the final tag name")
                append("note: the user starts the deploy in the IDE; the pending annotation takes precedence over the default annotation.")
            }
        }
    }

    private fun repository(project: Project): GitRepository =
        PendingAnnotation.repoForProject(project) ?: mcpFail("No Git repository found in project ${project.name}.")

    private fun annotationFile(project: Project): File =
        PendingAnnotation.file(project, repository(project))
            ?: mcpFail("Could not determine the Git directory of project ${project.name}.")
}
