package id.ardion.quran.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.domain.model.LastRead
import id.ardion.quran.domain.model.MushafLastRead
import id.ardion.quran.domain.model.SurahLastRead
import id.ardion.quran.presentation.theme.IslamicBackground
import id.ardion.quran.presentation.theme.IslamicGold
import id.ardion.quran.presentation.theme.IslamicPrimary
import id.ardion.quran.presentation.theme.IslamicTextPrimary
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardContent(
    lastRead: LastRead,
    onOpenSurahList: () -> Unit,
    onOpenMushaf: () -> Unit,
    onOpenPrayerTimes: () -> Unit,
    onContinueSurah: (surahNumber: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        GreetingCard(
            lastRead = lastRead,
            onContinueSurah = onContinueSurah,
            onContinueMushaf = onOpenMushaf
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.choose_menu),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = IslamicTextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            item {
                MenuCard(
                    title = stringResource(Res.string.menu_surah_title),
                    subtitle = stringResource(Res.string.menu_surah_sub),
                    icon = Icons.AutoMirrored.Filled.List,
                    onClick = onOpenSurahList
                )
            }
            item {
                MenuCard(
                    title = stringResource(Res.string.menu_mushaf_title),
                    subtitle = stringResource(Res.string.menu_mushaf_sub),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    onClick = onOpenMushaf
                )
            }
            item {
                MenuCard(
                    title = stringResource(Res.string.menu_prayer_title),
                    subtitle = stringResource(Res.string.menu_prayer_sub),
                    icon = Icons.Default.CompassCalibration,
                    onClick = onOpenPrayerTimes
                )
            }
        }
    }
}

@Composable
private fun GreetingCard(
    lastRead: LastRead,
    onContinueSurah: (Int) -> Unit,
    onContinueMushaf: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = IslamicPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.greeting),
                        color = IslamicGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(Res.string.app_title),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Image(
                    painter = painterResource(Res.drawable.bacalah_mark),
                    contentDescription = null,
                    modifier = Modifier.size(width = 44.dp, height = 42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                lastRead.surah?.let { surah ->
                    LastReadCard(
                        title = stringResource(Res.string.last_read_surah_title),
                        detail = stringResource(Res.string.surah_last_read_format, surah.surahName, surah.ayahNumber),
                        onClick = { onContinueSurah(surah.surahNumber) }
                    )
                }
                lastRead.mushaf?.let { mushaf ->
                    LastReadCard(
                        title = stringResource(Res.string.last_read_mushaf_title),
                        detail = mushafLastReadText(mushaf),
                        onClick = onContinueMushaf
                    )
                }
            }
        }
    }
}

@Composable
private fun mushafLastReadText(mushaf: MushafLastRead): String {
    val surahName = mushaf.surahName
    val ayahNumber = mushaf.ayahNumber
    return when {
        surahName != null && ayahNumber != null ->
            stringResource(Res.string.mushaf_last_read_format, mushaf.pageNumber, surahName, ayahNumber)
        surahName != null -> stringResource(Res.string.mushaf_last_read_format_simple, mushaf.pageNumber, surahName)
        else -> stringResource(Res.string.mushaf_last_read_format_page, mushaf.pageNumber)
    }
}

@Preview
@Composable
private fun DashboardContentPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(IslamicBackground)) {
            DashboardContent(
                lastRead = LastRead(
                    surah = SurahLastRead(surahNumber = 36, surahName = "Yasin", ayahNumber = 15),
                    mushaf = MushafLastRead(pageNumber = 50, surahName = "Al-Baqarah", ayahNumber = 10)
                ),
                onOpenSurahList = {},
                onOpenMushaf = {},
                onOpenPrayerTimes = {},
                onContinueSurah = {}
            )
        }
    }
}
