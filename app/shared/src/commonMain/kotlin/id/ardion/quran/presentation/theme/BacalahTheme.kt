package id.ardion.quran.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import id.ardion.quran.presentation.theme.IslamicBackground
import id.ardion.quran.presentation.theme.IslamicCardHighlight
import id.ardion.quran.presentation.theme.IslamicGold
import id.ardion.quran.presentation.theme.IslamicPrimary
import id.ardion.quran.presentation.theme.IslamicSurface
import id.ardion.quran.presentation.theme.IslamicTextPrimary

private val BacalahColorScheme = lightColorScheme(
    primary = IslamicPrimary,
    onPrimary = Color.White,
    primaryContainer = IslamicCardHighlight,
    onPrimaryContainer = IslamicPrimary,
    secondary = IslamicGold,
    onSecondary = Color.White,
    background = IslamicBackground,
    onBackground = IslamicTextPrimary,
    surface = IslamicSurface,
    onSurface = IslamicTextPrimary,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = IslamicBackground
)

@Composable
fun BacalahTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = BacalahColorScheme, content = content)
}
