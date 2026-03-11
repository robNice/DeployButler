package de.robnice.deploybutler.settings

import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField

class DeployChecksEditorPanel : JPanel(BorderLayout()) {

    private val rowsPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    init {
        val controls = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            add(JButton("+").apply {
                toolTipText = "Add check"
                addActionListener { addRow("") }
            })
        }

        add(controls, BorderLayout.NORTH)
        add(JBScrollPane(rowsPanel).apply {
            preferredSize = Dimension(420, 180)
        }, BorderLayout.CENTER)
    }

    fun setChecks(checks: List<String>) {
        rowsPanel.removeAll()
        if (checks.isEmpty()) {
            addRow("")
        } else {
            checks.forEach { addRow(it) }
        }
        refreshUi()
    }

    fun getChecks(): MutableList<String> =
        rowsPanel.components
            .mapNotNull { it as? CheckRowPanel }
            .map { it.getValue().trim() }
            .filter { it.isNotBlank() }
            .toMutableList()

    private fun addRow(value: String) {
        rowsPanel.add(CheckRowPanel(value))
        refreshUi()
    }

    private fun removeRow(row: CheckRowPanel) {
        rowsPanel.remove(row)
        if (rowsPanel.componentCount == 0) {
            addRow("")
        }
        refreshUi()
    }

    private fun moveRowUp(row: CheckRowPanel) {
        val index = rowsPanel.components.indexOf(row)
        if (index > 0) {
            rowsPanel.remove(index)
            rowsPanel.add(row, index - 1)
            refreshUi()
        }
    }

    private fun moveRowDown(row: CheckRowPanel) {
        val index = rowsPanel.components.indexOf(row)
        if (index >= 0 && index < rowsPanel.componentCount - 1) {
            rowsPanel.remove(index)
            rowsPanel.add(row, index + 1)
            refreshUi()
        }
    }

    private fun refreshUi() {
        rowsPanel.revalidate()
        rowsPanel.repaint()
        revalidate()
        repaint()
    }

    private inner class CheckRowPanel(value: String) : JPanel(BorderLayout(8, 0)) {
        private val textField = JTextField(value)

        init {
            val buttons = JPanel(FlowLayout(FlowLayout.LEFT, 4, 0)).apply {
                add(JButton("↑").apply {
                    toolTipText = "Move up"
                    addActionListener { moveRowUp(this@CheckRowPanel) }
                })
                add(JButton("↓").apply {
                    toolTipText = "Move down"
                    addActionListener { moveRowDown(this@CheckRowPanel) }
                })
                add(JButton("−").apply {
                    toolTipText = "Remove check"
                    addActionListener { removeRow(this@CheckRowPanel) }
                })
            }

            add(textField, BorderLayout.CENTER)
            add(buttons, BorderLayout.EAST)
            add(Box.createVerticalStrut(4), BorderLayout.SOUTH)
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        }

        fun getValue(): String = textField.text
    }
}