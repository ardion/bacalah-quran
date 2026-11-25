@file:OptIn(ExperimentalWasmJsInterop::class)

package id.ardion.quran.logging

private fun consoleWarn(message: String): Unit = js("console.warn(message)")

actual fun logWarning(tag: String, message: String, throwable: Throwable?) {
    val detail = throwable?.let { " — $it" }.orEmpty()
    consoleWarn("[$tag] $message$detail")
}
