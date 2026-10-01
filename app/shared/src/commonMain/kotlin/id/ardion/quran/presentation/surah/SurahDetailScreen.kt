package id.ardion.quran.presentation.surah

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.Res
import bacalah.app.shared.generated.resources.amiri
import id.ardion.quran.constants.QuranConstants
import id.ardion.quran.domain.manager.LastReadManager
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.presentation.components.ErrorScreen
import id.ardion.quran.presentation.components.SurahDetailShimmer
import id.ardion.quran.state.UiState
import id.ardion.quran.theme.IslamicBackground
import id.ardion.quran.theme.IslamicCardHighlight
import id.ardion.quran.theme.IslamicGold
import id.ardion.quran.theme.IslamicPrimary
import id.ardion.quran.theme.IslamicTextPrimary
import id.ardion.quran.theme.IslamicTextSecondary
import id.ardion.quran.utils.TajweedInfo
import id.ardion.quran.utils.getTajweedInfo
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    viewModel: SurahDetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val playingUrl by viewModel.playingAudioUrl.collectAsState()
    val isPlayingFullSurah by viewModel.isPlayingFullSurah.collectAsState()
    val multiLastRead by LastReadManager.multiLastRead.collectAsState()
    val surahLastRead = multiLastRead.surahLastRead
    var selectedTajweedInfo by remember { mutableStateOf<TajweedInfo?>(null) }
    val listState = rememberLazyListState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onCleared()
        }
    }

    (state as? UiState.Success)?.data?.let { surahDetail ->
        LaunchedEffect(surahDetail.number) {
            if (surahLastRead != null &&
                surahLastRead.surahNumber == surahDetail.number &&
                surahLastRead.ayahNumber > 0
            ) {
                val targetIndex = (surahLastRead.ayahNumber - 1).coerceAtLeast(0)
                delay(150)
                listState.scrollToItem(targetIndex + 1)
            }
        }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (state as? UiState.Success)?.data?.englishName ?: "Detail Surah",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = QuranConstants.DESC_NAV_BACK,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IslamicPrimary
                )
            )
        },
        containerColor = IslamicBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val current = state) {
                is UiState.Idle, is UiState.Loading -> SurahDetailShimmer()

                is UiState.Error -> ErrorScreen(
                    message = current.message,
                    onRetry = {
                        viewModel.loadSurah(1)
                    }
                )

                is UiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = IslamicPrimary),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (current.data.number != 9) {
                                        Text(
                                            text = QuranConstants.BISMILLAH_TEXT,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontFamily = FontFamily(Font(Res.font.amiri))
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                    Button(
                                        onClick = { viewModel.togglePlayFullSurah(current.data.ayahs) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isPlayingFullSurah) IslamicGold else Color.White.copy(alpha = 0.25f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingFullSurah) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isPlayingFullSurah) "Jeda Surah" else "Putar Seluruh Surah",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        items(current.data.ayahs) { ayah ->
                            val isLastRead = surahLastRead != null &&
                                    surahLastRead.surahNumber == current.data.number &&
                                    surahLastRead.ayahNumber == ayah.numberInSurah

                            AyahDetailItem(
                                ayah = ayah,
                                isPlaying = playingUrl == ayah.audioUrl,
                                isLastRead = isLastRead,
                                onPlayAudio = { url -> viewModel.playAudio(url) },
                                onBookmarkClick = {
                                    if (isLastRead) {
                                        LastReadManager.clearSurahLastRead()
                                    } else {
                                        LastReadManager.saveLastReadSurah(
                                            surahNumber = current.data.number,
                                            surahName = current.data.englishName,
                                            ayahNumber = ayah.numberInSurah
                                        )
                                    }
                                },
                                onTajweedClick = { info -> selectedTajweedInfo = info }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AyahDetailItem(
    ayah: AyahDetail,
    isPlaying: Boolean,
    isLastRead: Boolean,
    onPlayAudio: (String) -> Unit,
    onBookmarkClick: () -> Unit,
    onTajweedClick: (TajweedInfo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying || isLastRead) IslamicCardHighlight else Color.White
        ),
        border = if (isPlaying || isLastRead) BorderStroke(1.5.dp, IslamicGold) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying || isLastRead) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying || isLastRead) IslamicGold else IslamicPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${ayah.numberInSurah}",
                            color = if (isPlaying || isLastRead) Color.White else IslamicPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (isLastRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = IslamicGold,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = " Terakhir Dibaca",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isLastRead) IslamicGold.copy(alpha = 0.2f) else IslamicPrimary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = if (isLastRead) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Tandai Terakhir Dibaca",
                            tint = if (isLastRead) IslamicGold else IslamicPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    ayah.audioUrl?.let { url ->
                        IconButton(
                            onClick = { onPlayAudio(url) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) IslamicPrimary else IslamicPrimary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) QuranConstants.DESC_PAUSE_AUDIO else QuranConstants.DESC_PLAY_AUDIO,
                                tint = if (isPlaying) Color.White else IslamicPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ClickableText(
                text = ayah.textTajweed,
                onClick = { offset ->
                    ayah.textTajweed
                        .getStringAnnotations(tag = "TAJWEED", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            val info = getTajweedInfo(annotation.item)
                            onTajweedClick(info)
                        }
                },
                style = LocalTextStyle.current.copy(
                    fontFamily = FontFamily(
                        Font(Res.font.amiri)
                    ),
                    fontSize = 28.sp,
                    lineHeight = 56.sp,
                    textAlign = TextAlign.End,
                    color = Color(0xFF1A1A1A),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            )

            ayah.transliteration?.let { transliteration ->
                if (transliteration.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = transliteration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC5A059),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )
                }
            }

            if (ayah.translation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Terjemahan",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ayah.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IslamicTextSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
