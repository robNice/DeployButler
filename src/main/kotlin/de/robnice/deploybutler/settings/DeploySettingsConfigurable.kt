package de.robnice.deploybutler.settings

import de.robnice.deploybutler.i18n.message
import com.intellij.icons.AllIcons
import com.intellij.ide.HelpTooltip
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.panel
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.vfs.VfsUtil
import java.awt.Font
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTextField
import javax.swing.UIManager

class DeploySettingsConfigurable(
    private val project: Project
) : Configurable {

    private val settings = project.service<DeploySettingsState>()
    private val dryRunCheckbox = JCheckBox(message("settings.dryRun"))
    private val rebaseCheckbox = JCheckBox(message("settings.rebase"))
    private val confirmCheckbox = JCheckBox(message("settings.confirm"))
    private val branchField = JTextField()
    private val remoteField = JTextField()
    private val prefixField = JTextField()
    private val preferredDetectorCombo = ComboBox(arrayOf("", "gradle", "maven", "package-json", "composer", "custom-regex"))
    private val customPathField = TextFieldWithBrowseButton()
    private val customRegexField = JTextField().apply {
        columns = 40
    }
    private val deployChecksEditor = DeployChecksEditorPanel()

    init {
        setupCustomPathChooser()
    }

    override fun createComponent(): JComponent =
        panel {
            group(message("settings.section.general")) {
                row {
                    cell(dryRunCheckbox)
                }
                helpRow("settings.help.dryRun")

                row(message("settings.branch")) {
                    cell(branchField).resizableColumn()
                }
                helpRow("settings.help.branch")

                row(message("settings.remote")) {
                    cell(remoteField).resizableColumn()
                }
                helpRow("settings.help.remote")

                row(message("settings.prefix")) {
                    cell(prefixField).resizableColumn()
                }
                helpRow("settings.help.prefix")

                row {
                    cell(rebaseCheckbox)
                }
                helpRow("settings.help.rebase")

                row {
                    cell(confirmCheckbox)
                }
                helpRow("settings.help.confirm")
            }

            group(message("settings.section.versionDetection")) {
                row(message("settings.versionDetector")) {
                    cell(preferredDetectorCombo).resizableColumn()
                }
                helpRow("settings.help.versionDetector")

                row(message("settings.versionCustomPath")) {
                    cell(customPathField).resizableColumn()
                }
                helpRow("settings.help.versionCustomPath")

                row(message("settings.versionCustomRegex")) {
                    cell(customRegexField).resizableColumn()
                }
                helpRow("settings.help.versionCustomRegex")
            }

            group(message("settings.section.deployChecks")) {
                row {
                    label(message("settings.deployChecks"))
                        .align(AlignY.TOP)

                    cell(deployChecksEditor)
                        .resizableColumn()
                        .align(AlignY.TOP)

                    cell(helpIcon("settings.help.deployChecks"))
                        .align(AlignY.TOP)
                }
            }
        }

    private fun setupCustomPathChooser() {
        val basePath = project.basePath ?: return
        val baseDir = Paths.get(basePath).normalize()
        val baseVirtualFile = LocalFileSystem.getInstance().findFileByPath(baseDir.toString()) ?: return

        val descriptor = object : FileChooserDescriptor(
            true,   // chooseFiles
            false,  // chooseFolders
            false,  // chooseJars
            false,  // chooseJarsAsFiles
            false,  // chooseJarContents
            false   // chooseMultiple
        ) {
            override fun isFileSelectable(file: VirtualFile?): Boolean {
                if (file == null || file.isDirectory) return false
                return isInsideProject(baseDir, file)
            }

            override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean): Boolean {
                if (!super.isFileVisible(file, showHiddenFiles)) return false
                return file.path == baseDir.toString() || isInsideProject(baseDir, file)
            }
        }

        descriptor.title = message("settings.versionCustomPath")
        descriptor.description = message("settings.help.versionCustomPath")
        descriptor.roots = listOf(baseVirtualFile)

        customPathField.textField.columns = 40

        customPathField.addActionListener {
            val currentText = customPathField.text.trim()

            val initialFile = resolveInitialFile(currentText, baseDir)
                ?: baseVirtualFile

            val selected = FileChooser.chooseFile(descriptor, project, initialFile)
                ?: return@addActionListener

            customPathField.text = baseDir
                .relativize(Paths.get(selected.path).normalize())
                .toString()
                .replace('\\', '/')
        }
    }

    private fun resolveInitialFile(rawText: String, baseDir: Path): VirtualFile? {
        if (rawText.isBlank()) return null

        val path = runCatching {
            val input = Paths.get(rawText)
            val absolute = if (input.isAbsolute) input.normalize() else baseDir.resolve(input).normalize()
            if (!absolute.startsWith(baseDir)) return null
            LocalFileSystem.getInstance().findFileByPath(absolute.toString())
        }.getOrNull()

        return path
    }

    private fun isInsideProject(baseDir: Path, file: VirtualFile): Boolean =
        runCatching {
            Paths.get(file.path).normalize().startsWith(baseDir)
        }.getOrDefault(false)

    private fun toRelativeProjectPath(rawPath: String): String {
        val trimmed = rawPath.trim()
        if (trimmed.isBlank()) return ""

        val basePath = project.basePath ?: return trimmed
        val baseDir = Paths.get(basePath).normalize()
        val inputPath = Paths.get(trimmed)

        val absolutePath = if (inputPath.isAbsolute) {
            inputPath.normalize()
        } else {
            baseDir.resolve(inputPath).normalize()
        }

        require(absolutePath.startsWith(baseDir)) {
            "Selected file must be inside the project directory."
        }

        return baseDir.relativize(absolutePath).toString().replace('\\', '/')
    }

    private fun com.intellij.ui.dsl.builder.Panel.helpRow(messageKey: String) {
        row {
            label(message(messageKey))
                .applyToComponent {
                    foreground = UIManager.getColor("ContextHelp.FOREGROUND")
                        ?: UIManager.getColor("Label.disabledForeground")
                                ?: JBColor.GRAY

                    font = font.deriveFont(Font.PLAIN, font.size2D - 1f)
                }
        }
    }

    private fun helpIcon(messageKey: String): JLabel =
        JLabel(AllIcons.General.ContextHelp).apply {
            HelpTooltip()
                .setDescription(message(messageKey))
                .installOn(this)
        }

    override fun reset() {
        branchField.text = settings.targetBranch
        remoteField.text = settings.remoteName
        prefixField.text = settings.tagPrefix
        rebaseCheckbox.isSelected = settings.useRebase
        confirmCheckbox.isSelected = settings.confirmationsEnabled
        dryRunCheckbox.isSelected = settings.dryRunEnabled

        preferredDetectorCombo.selectedItem = settings.preferredVersionDetector
        customPathField.text = settings.versionCustomPath
        customRegexField.text = settings.versionCustomRegex
        deployChecksEditor.setChecks(settings.deployChecks)
    }

    override fun isModified(): Boolean =
        branchField.text.trim() != settings.targetBranch ||
                remoteField.text.trim() != settings.remoteName ||
                prefixField.text.trim() != settings.tagPrefix ||
                rebaseCheckbox.isSelected != settings.useRebase ||
                confirmCheckbox.isSelected != settings.confirmationsEnabled ||
                dryRunCheckbox.isSelected != settings.dryRunEnabled ||
                (preferredDetectorCombo.selectedItem as? String ?: "").trim() != settings.preferredVersionDetector ||
                customPathField.text.trim() != settings.versionCustomPath ||
                customRegexField.text.trim() != settings.versionCustomRegex ||
                deployChecksEditor.getChecks() != settings.deployChecks

    override fun apply() {
        settings.targetBranch = branchField.text.trim().ifBlank { "main" }
        settings.remoteName = remoteField.text.trim().ifBlank { "origin" }
        settings.tagPrefix = prefixField.text.trim().ifBlank { "v" }
        settings.useRebase = rebaseCheckbox.isSelected
        settings.confirmationsEnabled = confirmCheckbox.isSelected
        settings.dryRunEnabled = dryRunCheckbox.isSelected

        settings.preferredVersionDetector = (preferredDetectorCombo.selectedItem as? String ?: "").trim()
        settings.versionCustomPath = toRelativeProjectPath(customPathField.text)
        settings.versionCustomRegex = customRegexField.text
        settings.deployChecks = deployChecksEditor.getChecks()
    }

    override fun getDisplayName(): String = message("settings.title")
}