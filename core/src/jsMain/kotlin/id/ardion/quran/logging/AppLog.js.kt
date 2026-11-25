package id.ardion.quran.logging

actual fun logWarning(tag: String, message: String, throwable: Throwable?) {
    console.warn("[$tag] $message", throwable?.toString().orEmpty())
}
