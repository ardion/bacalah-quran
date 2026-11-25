package id.ardion.quran.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.presentation.theme.IslamicBackground
import id.ardion.quran.presentation.theme.IslamicGold
import id.ardion.quran.presentation.theme.IslamicPrimary
import org.jetbrains.compose.resources.*

private const val LOGO_START_SCALE = 0.85f
private const val LOGO_ENTRANCE_MS = 600

@Composable
fun SplashScreen() {
    val scale = remember { Animatable(LOGO_START_SCALE) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(durationMillis = LOGO_ENTRANCE_MS, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.bacalah_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(112.dp)
                    .scale(scale.value)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.app_name),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(Res.string.app_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = IslamicGold
            )
        }
    }
}
