package id.ardion.quran.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.Res
import bacalah.app.shared.generated.resources.amiri
import id.ardion.quran.constants.QuranConstants
import id.ardion.quran.theme.IslamicBackground
import id.ardion.quran.theme.IslamicGold
import id.ardion.quran.theme.IslamicPrimary
import org.jetbrains.compose.resources.Font

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
                    text = "اقْرَأْ",
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
                text = "Bacalah",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = QuranConstants.APP_TITLE,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = IslamicGold
            )
        }
    }
}
