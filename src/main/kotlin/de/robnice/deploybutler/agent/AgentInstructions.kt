package de.robnice.deploybutler.agent

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import de.robnice.deploybutler.git.PendingAnnotation

/**
 * Writes a marked DeployButler section into an agent instruction file (AGENTS.md, CLAUDE.md)
 * in the project root, so AI agents learn how to pre-fill the tag annotation.
 */
object AgentInstructions {
    const val AGENTS_MD = "AGENTS.md"
    const val CLAUDE_MD = "CLAUDE.md"

    private const val START = "<!-- deploybutler:start -->"
    private const val END = "<!-- deploybutler:end -->"

    enum class Result { CREATED, ADDED, UPDATED, UNCHANGED }

    val block: String = """
        $START
        ## DeployButler

        This project is deployed with the DeployButler IDE plugin. A deploy merges the current branch into the target branch and creates an annotated Git tag.

        To pre-fill the tag annotation (e.g. release notes) for the next deploy:

        - If the JetBrains IDE MCP tools are available, call `deploybutler_set_annotation`. `deploybutler_get_info` returns the latest tag, the next possible tags and the current settings.
        - Otherwise write the text (UTF-8) to the file `${PendingAnnotation.FILE_NAME}` in the Git directory: `"${'$'}(git rev-parse --git-dir)/${PendingAnnotation.FILE_NAME}"`.

        `${PendingAnnotation.TAG_PLACEHOLDER}` in the text is replaced with the final tag name. The file is deleted after the tag has been created. Do not start the deploy yourself; the user starts it in the IDE.
        $END
    """.trimIndent()

    /** Inserts or updates the DeployButler section. Must be called on the EDT. */
    fun upsert(project: Project, fileName: String): Result {
        val basePath = project.basePath ?: error("Project has no base path")
        val baseDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(basePath)
            ?: error("Project directory not found: $basePath")

        var result = Result.UNCHANGED
        WriteCommandAction.runWriteCommandAction(project, "DeployButler: $fileName", null, {
            val existing = baseDir.findChild(fileName)
            val file = existing ?: baseDir.createChildData(this, fileName)
            val document = FileDocumentManager.getInstance().getDocument(file)
                ?: error("Cannot open $fileName")
            val text = document.text
            val start = text.indexOf(START)
            val end = if (start >= 0) text.indexOf(END, start) else -1

            val newText = when {
                start >= 0 && end >= 0 -> text.substring(0, start) + block + text.substring(end + END.length)
                text.isBlank() -> block + "\n"
                else -> text.trimEnd() + "\n\n" + block + "\n"
            }

            result = when {
                newText == text -> Result.UNCHANGED
                existing == null -> Result.CREATED
                start >= 0 && end >= 0 -> Result.UPDATED
                else -> Result.ADDED
            }
            if (result != Result.UNCHANGED) {
                document.setText(newText)
                FileDocumentManager.getInstance().saveDocument(document)
            }
        })
        return result
    }
}
