package id.ardion.quran.platform

import id.ardion.quran.data.local.KeyValueStore
import kotlinx.browser.window

class LocalStorageKeyValueStore : KeyValueStore {
    override fun getString(key: String): String? = window.localStorage.getItem(key)
    override fun putString(key: String, value: String) = window.localStorage.setItem(key, value)
    override fun remove(key: String) = window.localStorage.removeItem(key)
}
