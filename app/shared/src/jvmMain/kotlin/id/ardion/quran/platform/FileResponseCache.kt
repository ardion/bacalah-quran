package id.ardion.quran.platform

import id.ardion.quran.data.local.ResponseCache
import id.ardion.quran.logging.logWarning
import java.io.File
import java.io.IOException

private const val TAG = "FileResponseCache"

class FileResponseCache(private val directory: File) : ResponseCache {

    override fun read(key: String): String? =
        try {
            fileFor(key).takeIf { it.exists() }?.readText()
        } catch (e: IOException) {
            logWarning(TAG, "Could not read $key", e)
            null
        }

    override fun write(key: String, value: String) {
        try {
            directory.mkdirs()
            val target = fileFor(key)
            val temp = File(directory, "${target.name}.tmp")
            temp.writeText(value)
            if (!temp.renameTo(target)) {
                target.delete()
                temp.renameTo(target)
            }
        } catch (e: IOException) {
            logWarning(TAG, "Could not write $key", e)
        }
    }

    override fun remove(key: String) {
        fileFor(key).delete()
    }

    private fun fileFor(key: String) = File(directory, "$key.json")
}
