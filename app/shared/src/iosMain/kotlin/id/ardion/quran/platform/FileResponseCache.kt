package id.ardion.quran.platform

import id.ardion.quran.data.local.ResponseCache
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

private const val CACHE_FOLDER = "quran-cache"

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class FileResponseCache : ResponseCache {
    private val fileManager = NSFileManager.defaultManager
    private val directory: String by lazy {
        val caches = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
            .firstOrNull() as? String ?: ""
        "$caches/$CACHE_FOLDER".also {
            fileManager.createDirectoryAtPath(it, withIntermediateDirectories = true, attributes = null, error = null)
        }
    }

    override fun read(key: String): String? =
        NSString.stringWithContentsOfFile(pathFor(key), encoding = NSUTF8StringEncoding, error = null)

    override fun write(key: String, value: String) {
        NSString.create(string = value).writeToFile(pathFor(key), atomically = true, encoding = NSUTF8StringEncoding, error = null)
    }

    override fun remove(key: String) {
        fileManager.removeItemAtPath(pathFor(key), error = null)
    }

    private fun pathFor(key: String) = "$directory/$key.json"
}
