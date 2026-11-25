package id.ardion.quran.platform

import id.ardion.quran.data.local.ResponseCache
import id.ardion.quran.logging.logWarning
import kotlinx.browser.window

private const val TAG = "LocalStorageResponseCache"
private const val KEY_PREFIX = "response_cache_"

class LocalStorageResponseCache : ResponseCache {

    override fun read(key: String): String? = window.localStorage.getItem(KEY_PREFIX + key)

    override fun write(key: String, value: String) {
        try {
            window.localStorage.setItem(KEY_PREFIX + key, value)
        } catch (e: Throwable) {
            logWarning(TAG, "Storage full, $key not cached", e)
        }
    }

    override fun remove(key: String) = window.localStorage.removeItem(KEY_PREFIX + key)
}
