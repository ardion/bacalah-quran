package id.ardion.quran.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.theme.IslamicBackground
import id.ardion.quran.theme.IslamicGold
import id.ardion.quran.theme.IslamicPrimary
import org.jetbrains.compose.resources.*

@Composable
fun SplashScreen() {
    val transition = rememberInfiniteTransition(label = "splashPulse")
    val scale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

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
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(IslamicPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.iqra_arabic),
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(
                        Font(Res.font.amiri)
                    )
                )
            }
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
