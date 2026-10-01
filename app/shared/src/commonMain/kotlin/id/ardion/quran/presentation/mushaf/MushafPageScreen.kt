package id.ardion.quran.presentation.mushaf

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.Res
import bacalah.app.shared.generated.resources.amiri
import id.ardion.quran.domain.manager.LastReadManager
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.domain.model.ReadMode
import id.ardion.quran.presentation.components.ErrorScreen
import id.ardion.quran.presentation.components.MushafPageShimmer
import id.ardion.quran.state.UiState
import id.ardion.quran.theme.IslamicBackground
import id.ardion.quran.theme.IslamicGold
import id.ardion.quran.theme.IslamicPrimary
import id.ardion.quran.theme.IslamicTextPrimary
import id.ardion.quran.theme.IslamicTextSecondary
import id.ardion.quran.utils.TajweedInfo
import id.ardion.quran.utils.getTajweedInfo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font

fun toArabicNumbers(number: Int): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { arabicDigits[it - '0'] }.joinToString("")
}

fun getJuzStartPage(juzNumber: Int): Int {
    if (juzNumber <= 1) return 1
    return (((juzNumber - 1) * 20) + 2).coerceIn(1, 604)
}

fun getSurahStartPage(surahNumber: Int): Int {
    val surahPages = mapOf(
        1 to 1, 2 to 2, 3 to 50, 4 to 77, 5 to 106, 6 to 128, 7 to 151, 8 to 177, 9 to 187, 10 to 208,
        18 to 293, 36 to 440, 55 to 531, 56 to 534, 67 to 562, 114 to 604
    )
    return surahPages[surahNumber] ?: run {
        ((surahNumber.toFloat() / 114f) * 604f).toInt().coerceIn(1, 604)
    }
}

@Composable
fun MushafPageScreen(
    viewModel: MushafPageViewModel,
    initialPageNumber: Int = 1
) {
    val initialPageIndex = (initialPageNumber - 1).coerceIn(0, 603)
    val pagerState = rememberPagerState(initialPage = initialPageIndex) { 604 }
    val currentPageNumber = pagerState.currentPage + 1
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentPageNumber) {
        viewModel.loadPage(currentPageNumber)
    }

    val state by viewModel.uiState.collectAsState()
    val playingUrl by viewModel.playingAudioUrl.collectAsState()
    val isPlayingPage by viewModel.isPlayingPage.collectAsState()
    val multiLastRead by LastReadManager.multiLastRead.collectAsState()
    val lastRead = multiLastRead.mushafLastRead
    var selectedTajweedInfo by remember { mutableStateOf<TajweedInfo?>(null) }
    var isFilterModalVisible by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onCleared()
        }
    }

    if (isFilterModalVisible) {
        var selectedTab by remember { mutableStateOf(0) }
        var filterInput by remember { mutableStateOf("$currentPageNumber") }

        AlertDialog(
            onDismissRequest = { isFilterModalVisible = false },
            title = {
                Text(
                    text = "Lompat Cepat (Filter Navigasi)",
                    fontWeight = FontWeight.Bold,
                    color = IslamicTextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFFF4F6F4),
                        contentColor = IslamicPrimary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0; filterInput = "$currentPageNumber" },
                            text = { Text("Halaman", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1; filterInput = "1" },
                            text = { Text("Surah", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2; filterInput = "1" },
                            text = { Text("Juz", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    val inputLabel = when (selectedTab) {
                        0 -> "Masukkan Nomor Halaman (1 - 604):"
                        1 -> "Masukkan Nomor Surah (1 - 114):"
                        else -> "Masukkan Nomor Juz (1 - 30):"
                    }

                    Text(
                        text = inputLabel,
                        fontSize = 13.sp,
                        color = IslamicTextSecondary
                    )

                    OutlinedTextField(
                        value = filterInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                val max = when (selectedTab) {
                                    0 -> 604
                                    1 -> 114
                                    else -> 30
                                }
                                val num = input.toIntOrNull() ?: 0
                                if (num <= max) filterInput = input
                            }
                        },
                        placeholder = { Text("Contoh: 1, 30, 604") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicPrimary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = filterInput.toIntOrNull()
                        if (num != null) {
                            val targetPage = when (selectedTab) {
                                0 -> num.coerceIn(1, 604)
                                1 -> getSurahStartPage(num)
                                else -> getJuzStartPage(num)
                            }
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetPage - 1)
                            }
                            isFilterModalVisible = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Buka Navigasi", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isFilterModalVisible = false }) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    selectedTajweedInfo?.let { tajweed ->
        AlertDialog(
            onDismissRequest = { selectedTajweedInfo = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(tajweed.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tajweed.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = tajweed.description,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedTajweedInfo = null }) {
                    Text("Tutup", color = IslamicPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(IslamicBackground)) {
        val currentJuz = (state as? UiState.Success)?.data?.ayahs?.firstOrNull()?.juz ?: 1

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = IslamicPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ) {
                    Text(
                        text = "Halaman $currentPageNumber, Juz $currentJuz",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info Halaman",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Filter Halaman, Surah, Juz",
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { isFilterModalVisible = true }
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().weight(1f),
            reverseLayout = true
        ) { pageIndex ->
            val pageNum = pageIndex + 1
            if (pageNum == currentPageNumber) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (val current = state) {
                        is UiState.Idle, is UiState.Loading -> MushafPageShimmer()

                        is UiState.Error -> ErrorScreen(
                            message = current.message,
                            onRetry = { viewModel.loadPage(pageNum) }
                        )

                        is UiState.Success -> {
                            MadaniMushafPageContent(
                                pageDetail = current.data,
                                playingUrl = playingUrl,
                                onPlayAudio = { url -> viewModel.playAudio(url) },
                                onTajweedClick = { info -> selectedTajweedInfo = info }
                            )
                        }
                    }
                }
            } else {
                MushafPageShimmer()
            }
        }

        (state as? UiState.Success)?.data?.ayahs?.let { ayahs ->
            if (ayahs.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Putar Halaman",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Halaman $currentPageNumber • ${ayahs.size} Ayat",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicTextPrimary
                            )
                        }

                        Button(
                            onClick = { viewModel.togglePlayPage(ayahs) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlayingPage || playingUrl != null) IslamicGold else Color(0xFFE2F1E7)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingPage || playingUrl != null) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isPlayingPage || playingUrl != null) Color.White else IslamicPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlayingPage || playingUrl != null) "Jeda" else "Putar",
                                color = if (isPlayingPage || playingUrl != null) Color.White else IslamicPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MadaniMushafPageContent(
    pageDetail: PageDetail,
    playingUrl: String?,
    onPlayAudio: (String) -> Unit,
    onTajweedClick: (TajweedInfo) -> Unit
) {
    val currentSurahName = pageDetail.ayahs.firstOrNull()?.surahName ?: "Al-Fatihah"
    val currentRevType = pageDetail.ayahs.firstOrNull()?.revelationType ?: "Makkiyah"
    val multiLastRead by LastReadManager.multiLastRead.collectAsState()
    val lastRead = multiLastRead.mushafLastRead

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF5ED)),
                border = BorderStroke(1.dp, IslamicPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Badge(
                        containerColor = IslamicPrimary.copy(alpha = 0.12f),
                        contentColor = IslamicPrimary
                    ) {
                        Text(
                            text = currentRevType,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "Surah $currentSurahName",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicPrimary
                    )

                    Badge(
                        containerColor = IslamicGold.copy(alpha = 0.15f),
                        contentColor = Color(0xFF8A6D2B)
                    ) {
                        Text(
                            text = "${pageDetail.ayahs.size} Ayat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        item {
            val pageAnnotatedString = remember(pageDetail.ayahs, lastRead) {
                buildAnnotatedString {
                    pushStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Center
                        )
                    )

                    pageDetail.ayahs.forEach { ayah ->
                        val startOffset = length
                        append(ayah.textTajweed)

                        ayah.textTajweed.getStringAnnotations(tag = "TAJWEED", start = 0, end = ayah.textTajweed.length)
                            .forEach { annotation ->
                                addStringAnnotation(
                                    tag = "TAJWEED",
                                    annotation = annotation.item,
                                    start = startOffset + annotation.start,
                                    end = startOffset + annotation.end
                                )
                            }

                        append(" ")

                        val isAyahBookmarked = lastRead != null &&
                                lastRead.pageNumber == pageDetail.pageNumber &&
                                lastRead.ayahNumber == ayah.numberInSurah

                        val badgeStart = length
                        val badgeText = "\u200F﴿${toArabicNumbers(ayah.numberInSurah)}﴾\u200F"

                        pushStyle(
                            SpanStyle(
                                color = if (isAyahBookmarked) Color.White else IslamicGold,
                                background = if (isAyahBookmarked) IslamicGold else Color.Transparent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        append(badgeText)
                        pop()

                        addStringAnnotation(
                            tag = "AYAH_MARKER",
                            annotation = "${ayah.numberInSurah}",
                            start = badgeStart,
                            end = badgeStart + badgeText.length
                        )

                        append(" ")
                    }
                    pop()
                }
            }

            var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

            Text(
                text = pageAnnotatedString,
                onTextLayout = { textLayoutResult = it },
                style = TextStyle(
                    fontFamily = FontFamily(Font(Res.font.amiri)),
                    fontSize = 26.sp,
                    lineHeight = 56.sp,
                    color = Color(0xFF1A1A1A)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .pointerInput(pageAnnotatedString) {
                        detectTapGestures { pos ->
                            textLayoutResult?.let { layout ->
                                val offset = layout.getOffsetForPosition(pos)

                                val ayahMarker = pageAnnotatedString
                                    .getStringAnnotations(tag = "AYAH_MARKER", start = offset, end = offset)
                                    .firstOrNull()

                                if (ayahMarker != null) {
                                    val clickedAyahNum = ayahMarker.item.toIntOrNull() ?: 1
                                    val isCurrentlyBookmarked = lastRead != null &&
                                            lastRead.pageNumber == pageDetail.pageNumber &&
                                            lastRead.ayahNumber == clickedAyahNum

                                    if (isCurrentlyBookmarked) {
                                        LastReadManager.clearMushafLastRead()
                                    } else {
                                        LastReadManager.saveLastReadPage(
                                            pageNumber = pageDetail.pageNumber,
                                            surahName = currentSurahName,
                                            ayahNumber = clickedAyahNum
                                        )
                                    }
                                } else {
                                    pageAnnotatedString
                                        .getStringAnnotations(tag = "TAJWEED", start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            val info = getTajweedInfo(annotation.item)
                                            onTajweedClick(info)
                                        }
                                }
                            }
                        }
                    }
            )
        }
    }
}
