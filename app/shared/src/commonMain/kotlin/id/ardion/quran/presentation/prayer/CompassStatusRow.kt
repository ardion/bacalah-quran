package id.ardion.quran.presentation.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.presentation.theme.IslamicGold
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CompassStatusRow(
    compass: CompassReading,
    onCalibrateClick: () -> Unit,
    onHowToUseClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            !compass.isAvailable -> StatusChip(
                icon = Icons.Default.Info,
                iconTint = Color.White.copy(alpha = 0.9f),
                text = stringResource(Res.string.compass_unavailable),
                textColor = Color.White.copy(alpha = 0.9f),
                background = Color.White.copy(alpha = 0.15f)
            )
            compass.accuracy < RELIABLE_SENSOR_ACCURACY -> StatusChip(
                icon = Icons.Default.Sync,
                iconTint = QiblaWarningIcon,
                text = stringResource(Res.string.lbl_calibration_instruction),
                textColor = QiblaWarningText,
                background = QiblaWarningBackground,
                bold = true,
                onClick = onCalibrateClick
            )
            !compass.isFlat -> StatusChip(
                icon = Icons.Default.ScreenRotation,
                iconTint = Color.White.copy(alpha = 0.9f),
                text = stringResource(Res.string.lbl_how_to_use_title),
                textColor = Color.White.copy(alpha = 0.9f),
                background = Color.White.copy(alpha = 0.15f)
            )
            else -> Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(QiblaSuccess))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.lbl_how_to_use_step),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onHowToUseClick)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(Res.string.instruction_cd),
                tint = IslamicGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.how_to_use_btn),
                color = IslamicGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatusChip(
    icon: ImageVector,
    iconTint: Color,
    text: String,
    textColor: Color,
    background: Color,
    bold: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (bold) FontWeight.Bold else null
        )
    }
}
