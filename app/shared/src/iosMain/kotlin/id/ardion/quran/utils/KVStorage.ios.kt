package id.ardion.quran.utils

import platform.Foundation.NSUserDefaults

actual object KVStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String): String? = defaults.stringForKey(key)
    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }
    actual fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}
