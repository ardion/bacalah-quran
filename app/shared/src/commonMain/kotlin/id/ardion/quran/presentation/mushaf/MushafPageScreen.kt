package id.ardion.quran.presentation.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.domain.model.MushafLastRead
import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.presentation.components.ErrorScreen
import id.ardion.quran.presentation.components.MushafPageShimmer
import id.ardion.quran.presentation.components.TajweedInfoDialog
import id.ardion.quran.state.UiState
import id.ardion.quran.presentation.theme.IslamicBackground
import id.ardion.quran.presentation.theme.IslamicGold
import id.ardion.quran.presentation.theme.IslamicPrimary
import id.ardion.quran.presentation.theme.IslamicTextPrimary
import id.ardion.quran.presentation.tajweed.TajweedInfo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private val PlayButtonIdleColor = Color(0xFFE2F1E7)

@Composable
fun MushafPageScreen(
    viewModel: MushafPageViewModel,
    onBackClick: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = (viewModel.initialPageNumber - 1).coerceIn(0, MUSHAF_PAGE_COUNT - 1)
    ) { MUSHAF_PAGE_COUNT }
    val currentPageNumber = pagerState.currentPage + 1
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentPageNumber) { viewModel.loadPage(currentPageNumber) }

    val state by viewModel.uiState.collectAsState()
    val playingUrl by viewModel.playingAudioUrl.collectAsState()
    val isPlayingPage by viewModel.isPlayingPage.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    var selectedTajweedInfo by remember { mutableStateOf<TajweedInfo?>(null) }
    var isJumpDialogVisible by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { viewModel.stopAudio() } }

    if (isJumpDialogVisible) {
        MushafJumpDialog(
            currentPageNumber = currentPageNumber,
            onJumpToPage = { page ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage((page - 1).coerceIn(0, MUSHAF_PAGE_COUNT - 1))
                }
                isJumpDialogVisible = false
            },
            onDismiss = { isJumpDialogVisible = false }
        )
    }

    selectedTajweedInfo?.let { info ->
        TajweedInfoDialog(info = info, onDismiss = { selectedTajweedInfo = null })
    }

    val pageDetail = (state as? UiState.Success)?.data

    Column(modifier = Modifier.fillMaxSize().background(IslamicBackground)) {
        MushafTopBar(onBackClick)

        MushafPageInfoBar(
            pageNumber = currentPageNumber,
            juz = pageDetail?.ayahs?.firstOrNull()?.juz,
            onOpenJumpDialog = { isJumpDialogVisible = true }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().weight(1f),
            reverseLayout = true
        ) { pageIndex ->
            val pageNum = pageIndex + 1
            if (pageNum != currentPageNumber) {
                MushafPageShimmer()
                return@HorizontalPager
            }
            MushafPageBody(
                state = state,
                bookmark = lastRead,
                onRetry = { viewModel.loadPage(pageNum) },
                onBookmarkToggle = viewModel::toggleLastRead,
                onTajweedClick = { info -> selectedTajweedInfo = info }
            )
        }

        pageDetail?.ayahs?.takeIf { it.isNotEmpty() && viewModel.isAudioAvailable }?.let { ayahs ->
            val isPlayingAudio by viewModel.isPlayingAudio.collectAsState()
            MushafPlayBar(
                pageNumber = currentPageNumber,
                ayahs = ayahs,
                isPlaying = (isPlayingPage || playingUrl != null) && isPlayingAudio,
                onTogglePlay = { viewModel.togglePlayPage(ayahs) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MushafTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                stringResource(Res.string.menu_mushaf_title),
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
}

@Composable
private fun MushafPageBody(
    state: UiState<PageDetail>,
    bookmark: MushafLastRead?,
    onRetry: () -> Unit,
    onBookmarkToggle: (pageNumber: Int, ayahNumber: Int, surahName: String?) -> Unit,
    onTajweedClick: (TajweedInfo) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            UiState.Loading -> MushafPageShimmer()
            is UiState.Error -> ErrorScreen(error = state.cause, onRetry = onRetry)
            is UiState.Success -> MushafPageContent(
                pageDetail = state.data,
                bookmark = bookmark,
                onBookmarkToggle = { ayah ->
                    onBookmarkToggle(state.data.pageNumber, ayah.numberInSurah, ayah.surahName)
                },
                onTajweedClick = onTajweedClick
            )
        }
    }
}

@Composable
private fun MushafPageInfoBar(pageNumber: Int, juz: Int?, onOpenJumpDialog: () -> Unit) {
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
                    text = if (juz != null) {
                        stringResource(Res.string.mushaf_page_juz_format, pageNumber, juz)
                    } else {
                        stringResource(Res.string.mushaf_page_format, pageNumber)
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            IconButton(onClick = onOpenJumpDialog) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = stringResource(Res.string.filter_page_cd),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun MushafPlayBar(
    pageNumber: Int,
    ayahs: List<AyahDetail>,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit
) {
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
                    text = stringResource(Res.string.mushaf_play_page),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = stringResource(Res.string.mushaf_page_ayah_count_format, pageNumber, ayahs.size),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicTextPrimary
                )
            }

            val contentColor = if (isPlaying) Color.White else IslamicPrimary
            Button(
                onClick = onTogglePlay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) IslamicGold else PlayButtonIdleColor
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(if (isPlaying) Res.string.mushaf_pause else Res.string.mushaf_play),
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
