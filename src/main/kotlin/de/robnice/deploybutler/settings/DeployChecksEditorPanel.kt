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
import javax.swing.KeyStroke

class DeployChecksEditorPanel : JPanel(BorderLayout()) {

    private val inputField = JTextField()

    private val rowsPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    init {
        val addButton = JButton("+").apply {
            toolTipText = "Add check"
            addActionListener { addFromInput() }
        }

        inputField.apply {
            columns = 30
            addActionListener { addFromInput() }
        }

        val controls = JPanel(BorderLayout(8, 0)).apply {
            add(inputField, BorderLayout.CENTER)
            add(addButton, BorderLayout.EAST)
        }

        add(controls, BorderLayout.NORTH)
        add(JBScrollPane(rowsPanel).apply {
            preferredSize = Dimension(420, 180)
        }, BorderLayout.CENTER)
    }

    fun setChecks(checks: List<String>) {
        rowsPanel.removeAll()
        checks
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { addRow(it) }
        refreshUi()
    }

    fun getChecks(): MutableList<String> =
        rowsPanel.components
            .mapNotNull { it as? CheckRowPanel }
            .map { it.getValue().trim() }
            .filter { it.isNotBlank() }
            .toMutableList()

    private fun addFromInput() {
        val value = inputField.text.trim()
        if (value.isBlank()) {
            inputField.requestFocusInWindow()
            return
        }

        addRow(value)
        inputField.text = ""
        inputField.requestFocusInWindow()
    }

    private fun addRow(value: String) {
        rowsPanel.add(CheckRowPanel(value))
        refreshUi()
    }

    private fun removeRow(row: CheckRowPanel) {
        rowsPanel.remove(row)
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

            textField.inputMap.put(KeyStroke.getKeyStroke("ENTER"), "none")
        }

        fun getValue(): String = textField.text
    }
}