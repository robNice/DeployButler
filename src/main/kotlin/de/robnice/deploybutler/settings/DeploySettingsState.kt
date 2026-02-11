package de.robnice.deploybutler.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "DeployButlerSettings",
    storages = [Storage("deploybutler.xml")]
)
class DeploySettingsState : PersistentStateComponent<DeploySettingsState> {

    var dryRunEnabled: Boolean = false
    var targetBranch: String = "live"
    var tagPrefix: String = "v"
    var useRebase: Boolean = false
    var confirmationsEnabled: Boolean = true

    override fun getState(): DeploySettingsState = this

    override fun loadState(state: DeploySettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
