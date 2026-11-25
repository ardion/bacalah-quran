package id.ardion.quran.presentation.prayer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ardion.quran.domain.model.PrayerTimes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import id.ardion.quran.presentation.theme.IslamicGold
import id.ardion.quran.presentation.theme.IslamicPrimary
import bacalah.app.shared.generated.resources.*
import kotlin.math.roundToLong
import org.jetbrains.compose.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LocationAndQiblaCard(
    prayerTimes: PrayerTimes,
    compass: CompassReading
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = IslamicPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LocationHeader(prayerTimes)

            Spacer(modifier = Modifier.height(16.dp))

            QiblaCompassView(
                qiblaBearing = prayerTimes.qiblaBearing,
                compass = compass
            )
        }
    }
}

@Composable
private fun LocationHeader(prayerTimes: PrayerTimes) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = IslamicGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.location_region),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Badge(
                containerColor = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White
            ) {
                Text(
                    text = formatDate(prayerTimes.date),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = prayerTimes.locationName ?: stringResource(Res.string.location_unknown),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        if (!prayerTimes.isLiveLocation) {
            Text(
                text = stringResource(Res.string.location_fallback_notice),
                color = IslamicGold,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stringResource(Res.string.coord_format, formatCoord(prayerTimes.latitude), formatCoord(prayerTimes.longitude)),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}

private const val COORDINATE_SCALE = 10_000.0

private fun formatCoord(value: Double): String = ((value * COORDINATE_SCALE).roundToLong() / COORDINATE_SCALE).toString()

@Composable
private fun formatDate(date: LocalDate): String {
    val monthNames = stringArrayResource(Res.array.month_short_names)
    val monthName = monthNames.getOrNull(date.month.number - 1).orEmpty()
    return stringResource(Res.string.date_format, date.day, monthName, date.year)
}
