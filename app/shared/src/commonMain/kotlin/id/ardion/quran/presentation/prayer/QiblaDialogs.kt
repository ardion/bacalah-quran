package id.ardion.quran.presentation.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.presentation.theme.IslamicPrimary
import id.ardion.quran.presentation.theme.IslamicTextPrimary
import id.ardion.quran.presentation.theme.IslamicTextSecondary
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun QiblaHowToUseDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.how_to_use_title),
                fontWeight = FontWeight.Bold,
                color = IslamicTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(Res.string.how_to_use_heading),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IslamicTextPrimary
                )
                StepList(
                    steps = listOf(Res.string.how_to_use_step1, Res.string.how_to_use_step2, Res.string.how_to_use_step3),
                    color = IslamicTextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.understand), color = IslamicPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun QiblaCalibrationDialog(sensorAccuracy: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = IslamicPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.calibration_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = IslamicTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FigureEightIllustration()
                SensorStatusChip(isReliable = sensorAccuracy >= RELIABLE_SENSOR_ACCURACY)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(Res.string.calibration_dialog_subtitle),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IslamicTextPrimary
                    )
                    StepList(
                        steps = listOf(
                            Res.string.calibration_dialog_step1,
                            Res.string.calibration_dialog_step2,
                            Res.string.calibration_dialog_step3
                        ),
                        color = IslamicTextPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.start_search_qibla),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    )
}

@Composable
private fun StepList(steps: List<StringResource>, color: Color) {
    steps.forEach { step ->
        Text(text = stringResource(step), fontSize = 12.sp, color = color)
    }
}

@Composable
private fun FigureEightIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(IslamicPrimary.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = IslamicPrimary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.AllInclusive, contentDescription = null, tint = IslamicPrimary, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(Res.string.calibration_instruction_head),
                fontSize = 11.sp,
                color = IslamicTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SensorStatusChip(isReliable: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isReliable) QiblaSuccessBackground else QiblaWarningBackground)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isReliable) QiblaSuccess else QiblaWarning)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(if (isReliable) Res.string.sensor_status_ready else Res.string.sensor_status_calibrating),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isReliable) QiblaSuccessDark else QiblaWarningText
        )
    }
}
