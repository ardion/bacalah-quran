package id.ardion.quran.presentation.prayer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.presentation.theme.IslamicGold
import id.ardion.quran.presentation.theme.IslamicPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CompassDial(qiblaBearing: Double, phoneAzimuth: Float, activeColor: Color) {
    val roseRotation = rememberContinuousRoseRotation(phoneAzimuth)

    Box(
        modifier = Modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .border(2.5.dp, activeColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = roseRotation),
            contentAlignment = Alignment.Center
        ) {
            CompassTicks()
            NorthMarker()
            CardinalLabel(stringResource(Res.string.cardinal_east_short), rotation = 90f)
            CardinalLabel(stringResource(Res.string.cardinal_south_short), rotation = 180f)
            CardinalLabel(stringResource(Res.string.cardinal_west_short), rotation = 270f)
            QiblaNeedle(bearing = qiblaBearing, color = activeColor)
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(top = 2.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                    .background(activeColor)
            )
        }

        HeadingReadout(phoneAzimuth = phoneAzimuth, accentColor = activeColor)
    }
}

@Composable
private fun rememberContinuousRoseRotation(phoneAzimuth: Float): Float {
    val target = (-phoneAzimuth + 360f) % 360f
    var continuous by remember { mutableStateOf(target) }

    LaunchedEffect(phoneAzimuth) {
        val current = (continuous % 360f + 360f) % 360f
        var step = target - current
        if (step > 180f) step -= 360f
        if (step < -180f) step += 360f
        continuous += step
    }

    val animated by animateFloatAsState(
        targetValue = continuous,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )
    return animated
}

@Composable
private fun CompassTicks() {
    Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        for (degree in 0 until 360 step 15) {
            val rad = (degree - 90) * (PI / 180.0)
            val isMajor = degree % 90 == 0
            val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
            val direction = Offset(cos(rad).toFloat(), sin(rad).toFloat())

            drawLine(
                color = if (isMajor) IslamicGold else Color.White.copy(alpha = 0.35f),
                start = center + direction * (radius - tickLength),
                end = center + direction * radius,
                strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
            )
        }
    }
}

@Composable
private fun DialMarker(rotation: Float, topPadding: Dp, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = rotation),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight().padding(top = topPadding),
            content = content
        )
    }
}

@Composable
private fun NorthMarker() {
    DialMarker(rotation = 0f, topPadding = 14.dp) {
        Text(
            text = stringResource(Res.string.cardinal_north_short),
            color = CompassNorth,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(CompassNorth)
        )
    }
}

@Composable
private fun CardinalLabel(text: String, rotation: Float) {
    DialMarker(rotation = rotation, topPadding = 16.dp) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QiblaNeedle(bearing: Double, color: Color) {
    DialMarker(rotation = bearing.toFloat(), topPadding = 6.dp) {
        KaabaVectorIcon(modifier = Modifier.size(22.dp))
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}

@Composable
private fun HeadingReadout(phoneAzimuth: Float, accentColor: Color) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(IslamicPrimary)
            .border(1.5.dp, accentColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${phoneAzimuth.toInt()}°",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = headingDirectionLabel(phoneAzimuth),
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun KaabaVectorIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        drawRoundRect(color = KaabaBlack, cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))
        drawRect(color = IslamicGold, topLeft = Offset(0f, h * 0.22f), size = Size(w, h * 0.14f))
        drawRoundRect(
            color = IslamicGold,
            topLeft = Offset(w * 0.58f, h * 0.48f),
            size = Size(w * 0.28f, h * 0.45f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

private val headingLabels = listOf(
    Res.string.heading_north, Res.string.heading_northeast, Res.string.heading_east, Res.string.heading_southeast,
    Res.string.heading_south, Res.string.heading_southwest, Res.string.heading_west, Res.string.heading_northwest
)

@Composable
private fun headingDirectionLabel(azimuth: Float): String {
    val normalized = (azimuth % 360f + 360f) % 360f
    val sectorWidth = 360f / headingLabels.size
    val sector = ((normalized + sectorWidth / 2) / sectorWidth).toInt() % headingLabels.size
    return stringResource(headingLabels[sector])
}
