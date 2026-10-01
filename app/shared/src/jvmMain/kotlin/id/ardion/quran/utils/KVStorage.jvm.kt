package id.ardion.quran.utils

import java.util.prefs.Preferences

actual object KVStorage {
    private val prefs = Preferences.userRoot().node("id.ardion.quran")

    actual fun getString(key: String): String? = prefs.get(key, null)
    actual fun putString(key: String, value: String) {
        prefs.put(key, value)
    }
    actual fun remove(key: String) {
        prefs.remove(key)
    }
}
