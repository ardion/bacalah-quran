package id.ardion.quran

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import id.ardion.quran.di.initKoin
import javax.imageio.ImageIO

private const val APP_ICON_RESOURCE = "app_icon.png"
private const val APP_NAME = "Bacalah"

fun main() {
    initKoin()
    application {
        val appIcon = remember {
            Thread.currentThread().contextClassLoader.getResourceAsStream(APP_ICON_RESOURCE)
                ?.use { BitmapPainter(ImageIO.read(it).toComposeImageBitmap()) }
        }
        Window(
            onCloseRequest = ::exitApplication,
            title = APP_NAME,
            icon = appIcon
        ) {
            App()
        }
    }
}
