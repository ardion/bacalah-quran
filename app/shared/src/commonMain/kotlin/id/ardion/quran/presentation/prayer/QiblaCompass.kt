package id.ardion.quran.presentation.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.presentation.theme.IslamicGold
import kotlin.math.abs
import kotlin.math.roundToLong
import org.jetbrains.compose.resources.stringResource

@Composable
fun QiblaCompassView(
    qiblaBearing: Double,
    compass: CompassReading,
    modifier: Modifier = Modifier
) {
    val phoneAzimuth = if (compass.isAvailable) compass.azimuth else 0f
    var showCalibrationDialog by rememberSaveable { mutableStateOf(compass.isAvailable) }
    var showHowToUseDialog by remember { mutableStateOf(false) }

    val turnAngle = ((qiblaBearing - phoneAzimuth + 540) % 360) - 180
    val turnDegrees = abs(turnAngle).toInt()
    val isFacingQibla = turnDegrees <= QIBLA_TOLERANCE_DEGREES
    val activeColor = if (isFacingQibla) QiblaSuccess else IslamicGold

    if (showHowToUseDialog) {
        QiblaHowToUseDialog(onDismiss = { showHowToUseDialog = false })
    }
    if (showCalibrationDialog) {
        QiblaCalibrationDialog(sensorAccuracy = compass.accuracy, onDismiss = { showCalibrationDialog = false })
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        CompassStatusRow(
            compass = compass,
            onCalibrateClick = { showCalibrationDialog = true },
            onHowToUseClick = { showHowToUseDialog = true }
        )
        Spacer(modifier = Modifier.height(10.dp))
        CompassDial(
            qiblaBearing = qiblaBearing,
            phoneAzimuth = phoneAzimuth,
            activeColor = activeColor
        )
        if (compass.isAvailable) {
            Spacer(modifier = Modifier.height(14.dp))
            QiblaGuidanceCard(
                isFacingQibla = isFacingQibla,
                turnRight = turnAngle > 0,
                turnDegrees = turnDegrees,
                qiblaBearing = qiblaBearing
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        QiblaTargetRow(qiblaBearing = qiblaBearing, onInfoClick = { showCalibrationDialog = true })
    }
}

@Composable
private fun QiblaGuidanceCard(
    isFacingQibla: Boolean,
    turnRight: Boolean,
    turnDegrees: Int,
    qiblaBearing: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFacingQibla) QiblaSuccessDark else Color.White.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFacingQibla) 3.dp else 0.dp)
    ) {
        if (isFacingQibla) {
            FacingQiblaRow(qiblaBearing)
        } else {
            TurnTowardsQiblaRow(turnRight = turnRight, turnDegrees = turnDegrees)
        }
    }
}

@Composable
private fun FacingQiblaRow(qiblaBearing: Double) {
    Row(
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = QiblaSuccessLight,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = stringResource(Res.string.lbl_qibla_facing_correct),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = stringResource(Res.string.qibla_facing_desc, formatDegree(qiblaBearing)),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun TurnTowardsQiblaRow(turnRight: Boolean, turnDegrees: Int) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(IslamicGold.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (turnRight) Icons.AutoMirrored.Filled.RotateRight else Icons.AutoMirrored.Filled.RotateLeft,
                contentDescription = null,
                tint = IslamicGold,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    if (turnRight) Res.string.qibla_turn_right else Res.string.qibla_turn_left,
                    turnDegrees
                ),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = stringResource(Res.string.qibla_align_instruction),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun QiblaTargetRow(qiblaBearing: Double, onInfoClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.qibla_target_label, formatDegree(qiblaBearing)),
            color = IslamicGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(
            onClick = onInfoClick,
            modifier = Modifier.size(22.dp).padding(start = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(Res.string.calibration_guide_cd),
                tint = IslamicGold.copy(alpha = 0.85f),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

private const val DEGREE_SCALE = 10.0

internal fun formatDegree(value: Double): String = ((value * DEGREE_SCALE).roundToLong() / DEGREE_SCALE).toString()
