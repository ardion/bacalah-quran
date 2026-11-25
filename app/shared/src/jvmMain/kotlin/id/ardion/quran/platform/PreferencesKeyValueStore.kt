package id.ardion.quran.platform

import id.ardion.quran.data.local.KeyValueStore
import java.util.prefs.Preferences

private const val PREFS_NODE = "id.ardion.quran"

class PreferencesKeyValueStore : KeyValueStore {
    private val prefs = Preferences.userRoot().node(PREFS_NODE)

    override fun getString(key: String): String? = prefs.get(key, null)
    override fun putString(key: String, value: String) = prefs.put(key, value)
    override fun remove(key: String) = prefs.remove(key)
}
