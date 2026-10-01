package id.ardion.quran.utils

import android.content.Context
import android.content.SharedPreferences

actual object KVStorage {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences("bacalah_quran_prefs", Context.MODE_PRIVATE)
        }
    }

    actual fun getString(key: String): String? = prefs?.getString(key, null)
    actual fun putString(key: String, value: String) {
        prefs?.edit()?.putString(key, value)?.apply()
    }
    actual fun remove(key: String) {
        prefs?.edit()?.remove(key)?.apply()
    }
}
