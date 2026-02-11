package de.robnice.deploybutler.git

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JComponent
import com.intellij.ui.components.JBScrollPane
import javax.swing.JTextArea

class DeployPreviewDialog(
    project: Project,
    previewText: String
) : DialogWrapper(project) {

    private val textArea = JTextArea(previewText).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        caretPosition = 0
    }

    init {
        title = "DeployButler – Dry Run"
        setOKButtonText("Proceed")
        setCancelButtonText("Cancel")
        init()
    }

    override fun createCenterPanel(): JComponent =
        JBScrollPane(textArea)

    override fun isOKActionEnabled(): Boolean = true
}
