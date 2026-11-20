package id.ardion.quran.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.domain.model.LastReadInfo
import id.ardion.quran.domain.model.MultiLastReadInfo
import id.ardion.quran.domain.model.ReadMode
import id.ardion.quran.presentation.mushaf.MushafPageScreen
import id.ardion.quran.presentation.mushaf.MushafPageViewModel
import id.ardion.quran.presentation.prayer.PrayerScreen
import id.ardion.quran.presentation.prayer.PrayerViewModel
import id.ardion.quran.presentation.surahlist.SurahListScreen
import id.ardion.quran.presentation.surahlist.SurahListViewModel
import id.ardion.quran.theme.IslamicBackground
import id.ardion.quran.theme.IslamicGold
import id.ardion.quran.theme.IslamicPrimary
import id.ardion.quran.theme.IslamicTextPrimary
import id.ardion.quran.theme.IslamicTextSecondary
import id.ardion.quran.utils.BackHandler
import org.jetbrains.compose.resources.*
import org.koin.compose.koinInject

enum class HomeSection {
    DASHBOARD,
    SURAH_LIST,
    MUSHAF_PAGE,
    PRAYER_TIMES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    listViewModel: SurahListViewModel,
    mushafViewModel: MushafPageViewModel,
    currentSection: HomeSection,
    onSectionChange: (HomeSection) -> Unit,
    onSurahClick: (Int) -> Unit
) {
    val multiLastRead by homeViewModel.multiLastRead.collectAsState()

    if (currentSection != HomeSection.DASHBOARD) {
        BackHandler(enabled = true) {
            onSectionChange(HomeSection.DASHBOARD)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(IslamicBackground)) {
        when (currentSection) {
            HomeSection.DASHBOARD -> DashboardContent(
                multiLastRead = multiLastRead,
                onOpenSurahList = { onSectionChange(HomeSection.SURAH_LIST) },
                onOpenMushaf = { onSectionChange(HomeSection.MUSHAF_PAGE) },
                onOpenPrayerTimes = { onSectionChange(HomeSection.PRAYER_TIMES) },
                onContinueReading = { lastReadInfo ->
                    if (lastReadInfo.readMode == ReadMode.MUSHAF) {
                        onSectionChange(HomeSection.MUSHAF_PAGE)
                    } else {
                        onSurahClick(lastReadInfo.surahNumber)
                    }
                }
            )

            HomeSection.SURAH_LIST -> SurahListScreen(
                viewModel = listViewModel,
                onSurahClick = onSurahClick,
                onBackClick = { onSectionChange(HomeSection.DASHBOARD) }
            )

            HomeSection.MUSHAF_PAGE -> Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(Res.string.menu_mushaf_title),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onSectionChange(HomeSection.DASHBOARD) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = IslamicPrimary)
                )
                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    val mushafPage = multiLastRead.mushafLastRead?.pageNumber ?: 1
                    MushafPageScreen(
                        viewModel = mushafViewModel,
                        initialPageNumber = mushafPage
                    )
                }
            }

            HomeSection.PRAYER_TIMES -> {
                val prayerViewModel: PrayerViewModel = koinInject()
                PrayerScreen(
                    viewModel = prayerViewModel,
                    onBackClick = { onSectionChange(HomeSection.DASHBOARD) }
                )
            }
        }
    }
}

@Composable
fun DashboardContent(
    multiLastRead: MultiLastReadInfo,
    onOpenSurahList: () -> Unit,
    onOpenMushaf: () -> Unit,
    onOpenPrayerTimes: () -> Unit,
    onContinueReading: (LastReadInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IslamicPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
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
                    Text(
                        text = stringResource(Res.string.iqra_arabic),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(Res.font.amiri))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    multiLastRead.surahLastRead?.let { surahLastRead ->
                        if (surahLastRead.surahNumber > 0) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onContinueReading(surahLastRead) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = IslamicGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = stringResource(Res.string.last_read_surah_title),
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = stringResource(
                                                    Res.string.surah_last_read_format,
                                                    surahLastRead.surahName,
                                                    surahLastRead.ayahNumber
                                                ),
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = stringResource(Res.string.continue_reading),
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    multiLastRead.mushafLastRead?.let { mushafLastRead ->
                        if (mushafLastRead.pageNumber > 0) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onContinueReading(mushafLastRead) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = IslamicGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = stringResource(Res.string.last_read_mushaf_title),
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 10.sp
                                            )
                                            val text = if (mushafLastRead.ayahNumber > 0) {
                                                stringResource(
                                                    Res.string.mushaf_last_read_format,
                                                    mushafLastRead.pageNumber,
                                                    mushafLastRead.surahName,
                                                    mushafLastRead.ayahNumber
                                                )
                                            } else {
                                                stringResource(
                                                    Res.string.mushaf_last_read_format_simple,
                                                    mushafLastRead.pageNumber,
                                                    mushafLastRead.surahName
                                                )
                                            }
                                            Text(
                                                text = text,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = stringResource(Res.string.continue_reading),
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

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
                    badgeText = stringResource(Res.string.badge_popular),
                    onClick = onOpenSurahList
                )
            }
            item {
                MenuCard(
                    title = stringResource(Res.string.menu_mushaf_title),
                    subtitle = stringResource(Res.string.menu_mushaf_sub),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    badgeText = stringResource(Res.string.badge_mushaf),
                    onClick = onOpenMushaf
                )
            }
            item {
                MenuCard(
                    title = stringResource(Res.string.menu_prayer_title),
                    subtitle = stringResource(Res.string.menu_prayer_sub),
                    icon = Icons.Default.CompassCalibration,
                    badgeText = stringResource(Res.string.badge_prayer),
                    onClick = onOpenPrayerTimes
                )
            }
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(IslamicPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = IslamicPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Badge(
                    containerColor = IslamicGold.copy(alpha = 0.15f),
                    contentColor = Color(0xFF8A6D2B)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IslamicTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun DashboardContentPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(IslamicBackground)) {
            DashboardContent(
                multiLastRead = MultiLastReadInfo(
                    surahLastRead = LastReadInfo(
                        readMode = ReadMode.SURAH,
                        surahNumber = 36,
                        surahName = "Yasin",
                        ayahNumber = 15
                    ),
                    mushafLastRead = LastReadInfo(
                        readMode = ReadMode.MUSHAF,
                        pageNumber = 50,
                        surahName = "Al-Baqarah",
                        ayahNumber = 10
                    )
                ),
                onOpenSurahList = {},
                onOpenMushaf = {},
                onOpenPrayerTimes = {},
                onContinueReading = {}
            )
        }
    }
}
