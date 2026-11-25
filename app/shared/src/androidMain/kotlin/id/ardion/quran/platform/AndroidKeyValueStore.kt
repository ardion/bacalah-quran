package id.ardion.quran.platform

import android.content.Context
import android.content.SharedPreferences
import id.ardion.quran.data.local.KeyValueStore

private const val PREFS_NAME = "bacalah_quran_prefs"

class AndroidKeyValueStore(context: Context) : KeyValueStore {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    override fun remove(key: String) = prefs.edit().remove(key).apply()
}
