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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.Res
import bacalah.app.shared.generated.resources.amiri
import id.ardion.quran.constants.QuranConstants
import id.ardion.quran.domain.manager.LastReadManager
import id.ardion.quran.domain.manager.MultiLastReadInfo
import id.ardion.quran.domain.model.LastReadInfo
import id.ardion.quran.domain.model.ReadMode
import id.ardion.quran.presentation.mushaf.MushafPageScreen
import id.ardion.quran.presentation.mushaf.MushafPageViewModel
import id.ardion.quran.presentation.surahlist.SurahListScreen
import id.ardion.quran.presentation.surahlist.SurahListViewModel
import id.ardion.quran.theme.IslamicBackground
import id.ardion.quran.theme.IslamicGold
import id.ardion.quran.theme.IslamicPrimary
import id.ardion.quran.theme.IslamicTextPrimary
import id.ardion.quran.theme.IslamicTextSecondary
import id.ardion.quran.utils.BackHandler
import org.jetbrains.compose.resources.Font

enum class HomeSection {
    DASHBOARD,
    SURAH_LIST,
    MUSHAF_PAGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    listViewModel: SurahListViewModel,
    mushafViewModel: MushafPageViewModel,
    currentSection: HomeSection,
    onSectionChange: (HomeSection) -> Unit,
    onSurahClick: (Int) -> Unit
) {
    val multiLastRead by LastReadManager.multiLastRead.collectAsState()

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
                            "Mushaf Madinah (604 Halaman)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onSectionChange(HomeSection.DASHBOARD) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali ke Beranda",
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
        }
    }
}

@Composable
fun DashboardContent(
    multiLastRead: MultiLastReadInfo,
    onOpenSurahList: () -> Unit,
    onOpenMushaf: () -> Unit,
    onContinueReading: (LastReadInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                            text = "Assalamu'alaikum",
                            color = IslamicGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = QuranConstants.APP_TITLE,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "اقْرَأْ",
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
                                                text = "Terakhir Dibaca (Surah)",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = "Surah ${surahLastRead.surahName} • Ayat ${surahLastRead.ayahNumber}",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Lanjutkan",
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
                                                text = "Terakhir Dibaca (Mushaf)",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 10.sp
                                            )
                                            val text = if (mushafLastRead.ayahNumber > 0) {
                                                "Mushaf • Halaman ${mushafLastRead.pageNumber} (${mushafLastRead.surahName} Ayat ${mushafLastRead.ayahNumber})"
                                            } else {
                                                "Mushaf • Halaman ${mushafLastRead.pageNumber} (${mushafLastRead.surahName})"
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
                                        contentDescription = "Lanjutkan",
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
            text = "Pilih Menu Bacaan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = IslamicTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                MenuCard(
                    title = "Daftar Surah",
                    subtitle = "114 Surah Lengkap",
                    icon = Icons.AutoMirrored.Filled.List,
                    badgeText = "Populer",
                    onClick = onOpenSurahList
                )
            }
            item {
                MenuCard(
                    title = "Mushaf Madinah",
                    subtitle = "604 Lembar Halaman",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    badgeText = "Swipe RTL",
                    onClick = onOpenMushaf
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
