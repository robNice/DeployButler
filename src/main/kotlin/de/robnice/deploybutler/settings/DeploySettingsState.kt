package de.robnice.deploybutler.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "DeployButlerSettings",
    storages = [Storage("deploybutler.xml")]
)
class DeploySettingsState : PersistentStateComponent<DeploySettingsState> {
    var dryRunEnabled: Boolean = true
    var targetBranch: String = "main"
    var remoteName: String = "origin"
    var tagPrefix: String = "v"
    var useRebase: Boolean = false
    var confirmationsEnabled: Boolean = true
    var versionCustomPath: String = ""
    var versionCustomRegex: String = ""
    var preferredVersionDetector: String = ""
    var deployChecks: MutableList<String> = mutableListOf()

    override fun getState(): DeploySettingsState = this

    override fun loadState(state: DeploySettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
