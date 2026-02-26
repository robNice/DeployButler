package de.robnice.deploybutler.git

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import de.robnice.deploybutler.i18n.message
import java.awt.Dimension
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.JTextArea

class DeployPreviewDialog(
    project: Project,
    previewText: String,
    proceedEnabled: Boolean
) : DialogWrapper(project) {

    private val textArea = JTextArea(previewText).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        caretPosition = 0
        margin = Insets(12, 12, 12, 12)
    }

    private val scrollPane = JBScrollPane(textArea).apply {
        preferredSize = Dimension(520, 360)
    }

    init {
        title = message("dialog.preview.title")
        if (proceedEnabled) {
            setOKButtonText(message("dialog.preview.proceed"))
            setCancelButtonText(message("dialog.preview.cancel"))
        } else {
            setOKButtonText(message("dialog.preview.close"))
            setCancelButtonText(message("dialog.preview.close"))
        }
        init()
    }

    override fun createCenterPanel(): JComponent = scrollPane
    override fun isOKActionEnabled(): Boolean = true
}