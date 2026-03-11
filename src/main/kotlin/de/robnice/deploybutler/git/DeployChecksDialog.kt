package de.robnice.deploybutler.git

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import de.robnice.deploybutler.i18n.message
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class DeployChecksDialog(
    project: Project,
    checks: List<String>
) : DialogWrapper(project) {

    private val checkboxes = checks.map { checkText ->
        JCheckBox(checkText).apply {
            addActionListener { updateOkState() }
        }
    }

    init {
        title = message("dialog.deployChecks.title")
        setOKButtonText(message("dialog.deployChecks.proceed"))
        setCancelButtonText(message("dialog.deployChecks.cancel"))
        init()
        updateOkState()
    }

    override fun createCenterPanel(): JComponent {
        val listPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            checkboxes.forEach { add(it) }
        }

        return JPanel(BorderLayout(0, 8)).apply {
            add(JLabel(message("dialog.deployChecks.description")), BorderLayout.NORTH)
            add(JBScrollPane(listPanel).apply {
                preferredSize = Dimension(480, 240)
            }, BorderLayout.CENTER)
        }
    }

    private fun updateOkState() {
        setOKActionEnabled(checkboxes.isNotEmpty() && checkboxes.all { it.isSelected })
    }
}