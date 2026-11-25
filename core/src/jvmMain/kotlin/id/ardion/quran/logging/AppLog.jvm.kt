package id.ardion.quran.logging

import java.util.logging.Level
import java.util.logging.Logger

actual fun logWarning(tag: String, message: String, throwable: Throwable?) {
    Logger.getLogger(tag).log(Level.WARNING, message, throwable)
}
