package id.ardion.quran.presentation.surah

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.model.SurahLastRead
import id.ardion.quran.presentation.components.ErrorScreen
import id.ardion.quran.presentation.components.SurahDetailShimmer
import id.ardion.quran.presentation.components.TajweedInfoDialog
import id.ardion.quran.state.UiState
import id.ardion.quran.presentation.theme.IslamicBackground
import id.ardion.quran.presentation.theme.IslamicGold
import id.ardion.quran.presentation.theme.IslamicPrimary
import id.ardion.quran.presentation.tajweed.TajweedInfo
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

private const val SURAH_WITHOUT_BISMILLAH = 9

private const val SCROLL_TO_LAST_READ_DELAY_MS = 150L

private const val HEADER_ITEM_COUNT = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    surahNumber: Int,
    viewModel: SurahDetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val playingUrl by viewModel.playingAudioUrl.collectAsState()
    val isPlayingAudio by viewModel.isPlayingAudio.collectAsState()
    val isPlayingFullSurah by viewModel.isPlayingFullSurah.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    var selectedTajweedInfo by remember { mutableStateOf<TajweedInfo?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(surahNumber) { viewModel.loadSurah(surahNumber) }
    DisposableEffect(Unit) { onDispose { viewModel.stopAudio() } }

    val surah = (state as? UiState.Success)?.data
    surah?.let { ScrollToLastReadEffect(it, lastRead, listState) }

    selectedTajweedInfo?.let { info ->
        TajweedInfoDialog(info = info, onDismiss = { selectedTajweedInfo = null })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = surah?.englishName ?: stringResource(Res.string.detail_surah_title),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IslamicPrimary)
            )
        },
        containerColor = IslamicBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val current = state) {
                UiState.Loading -> SurahDetailShimmer()
                is UiState.Error -> ErrorScreen(error = current.cause, onRetry = viewModel::retry)
                is UiState.Success -> SurahAyahList(
                    surah = current.data,
                    listState = listState,
                    lastRead = lastRead,
                    playingUrl = playingUrl,
                    isPlayingAudio = isPlayingAudio,
                    isPlayingFullSurah = isPlayingFullSurah,
                    onTogglePlayFullSurah = { viewModel.togglePlayFullSurah(current.data.ayahs) }
                        .takeIf { viewModel.isAudioAvailable },
                    onPlayAudio = viewModel::playAudio.takeIf { viewModel.isAudioAvailable },
                    onToggleBookmark = { ayah -> viewModel.toggleLastRead(current.data, ayah) },
                    onTajweedClick = { selectedTajweedInfo = it }
                )
            }
        }
    }
}

@Composable
private fun ScrollToLastReadEffect(surah: SurahDetail, lastRead: SurahLastRead?, listState: LazyListState) {
    LaunchedEffect(surah.number) {
        val bookmark = lastRead?.takeIf { it.surahNumber == surah.number } ?: return@LaunchedEffect
        val ayahIndex = surah.ayahs.indexOfFirst { it.numberInSurah == bookmark.ayahNumber }
        if (ayahIndex < 0) return@LaunchedEffect
        delay(SCROLL_TO_LAST_READ_DELAY_MS)
        val lastIndex = surah.ayahs.size - 1 + HEADER_ITEM_COUNT
        listState.scrollToItem((ayahIndex + HEADER_ITEM_COUNT).coerceIn(0, lastIndex))
    }
}

@Composable
private fun SurahAyahList(
    surah: SurahDetail,
    listState: LazyListState,
    lastRead: SurahLastRead?,
    playingUrl: String?,
    isPlayingAudio: Boolean,
    isPlayingFullSurah: Boolean,
    onTogglePlayFullSurah: (() -> Unit)?,
    onPlayAudio: ((String) -> Unit)?,
    onToggleBookmark: (AyahDetail) -> Unit,
    onTajweedClick: (TajweedInfo) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "header") {
            SurahHeaderCard(
                surah = surah,
                isPlayingFullSurah = isPlayingFullSurah,
                onTogglePlayFullSurah = onTogglePlayFullSurah
            )
        }

        items(surah.ayahs, key = { it.numberInSurah }) { ayah ->
            val isCurrentAyah = (playingUrl == ayah.audioUrl)
            AyahDetailItem(
                ayah = ayah,
                isPlaying = isCurrentAyah && isPlayingAudio,
                isCurrentActive = isCurrentAyah,
                isLastRead = lastRead?.surahNumber == surah.number && lastRead.ayahNumber == ayah.numberInSurah,
                onPlayAudio = onPlayAudio,
                onBookmarkClick = { onToggleBookmark(ayah) },
                onTajweedClick = onTajweedClick
            )
        }
    }
}

@Composable
private fun SurahHeaderCard(
    surah: SurahDetail,
    isPlayingFullSurah: Boolean,
    onTogglePlayFullSurah: (() -> Unit)?
) {
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
            if (surah.number != SURAH_WITHOUT_BISMILLAH) {
                Text(
                    text = stringResource(Res.string.bismillah_text),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily(Font(Res.font.amiri))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            onTogglePlayFullSurah?.let { onToggle ->
                Button(
                    onClick = onToggle,
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
                        text = stringResource(if (isPlayingFullSurah) Res.string.pause_full_surah else Res.string.play_full_surah),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
