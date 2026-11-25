package id.ardion.quran.logging

import platform.Foundation.NSLog

actual fun logWarning(tag: String, message: String, throwable: Throwable?) {
    val detail = throwable?.let { " — ${it::class.simpleName}: ${it.message}" }.orEmpty()
    NSLog("[%@] %@%@", tag, message, detail)
}
