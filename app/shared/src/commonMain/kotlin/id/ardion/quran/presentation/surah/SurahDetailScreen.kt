package id.ardion.quran.presentation.surah

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    viewModel: SurahDetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val playingUrl by viewModel.playingAudioUrl.collectAsState()
    var selectedTajweedInfo by remember { mutableStateOf<TajweedInfo?>(null) }

    selectedTajweedInfo?.let { tajweed ->
        AlertDialog(
            onDismissRequest = { selectedTajweedInfo = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(tajweed.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tajweed.name,
                            fontWeight = FontWeight.Bold,
                            color = IslamicTextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (tajweed.arabicName.isNotEmpty()) {
                        Text(
                            text = tajweed.arabicName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicPrimary,
                            fontFamily = FontFamily(Font(Res.font.amiri))
                        )
                    }
                }
            },
            text = {
                Text(
                    text = tajweed.description,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = IslamicTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedTajweedInfo = null }) {
                    Text(
                        text = QuranConstants.BTN_CLOSE,
                        color = IslamicPrimary,
                        fontWeight = FontWeight.Bold
                    )
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
                    val title = when (val current = state) {
                        is UiState.Success -> "${current.data.englishName} • ${current.data.name}"
                        else -> QuranConstants.DETAIL_SURAH_TITLE
                    }
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = if (state is UiState.Success) {
                            FontFamily(
                                Font(Res.font.amiri)
                            )
                        } else {
                            FontFamily.Default
                        }
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
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (current.data.number != 9) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = IslamicPrimary),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = QuranConstants.BISMILLAH_TEXT,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontFamily = FontFamily(
                                                Font(Res.font.amiri)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        items(current.data.ayahs) { ayah ->
                            AyahDetailItem(
                                ayah = ayah,
                                isPlaying = playingUrl == ayah.audioUrl,
                                onPlayAudio = { url -> viewModel.playAudio(url) },
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
    onPlayAudio: (String) -> Unit,
    onTajweedClick: (TajweedInfo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) IslamicCardHighlight else Color.White
        ),
        border = if (isPlaying) BorderStroke(1.5.dp, IslamicGold) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying) 6.dp else 2.dp)
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) IslamicPrimary else IslamicPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${ayah.numberInSurah}",
                        color = if (isPlaying) Color.White else IslamicPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                ayah.audioUrl?.let { url ->
                    IconButton(
                        onClick = { onPlayAudio(url) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) IslamicPrimary else IslamicPrimary.copy(alpha =.1f))
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
                    color = IslamicTextPrimary,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ayah.transliteration?.let { transliteration ->
                Text(
                    text = transliteration,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IslamicGold,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = QuranConstants.LABEL_TRANSLATION,
                style = MaterialTheme.typography.labelSmall,
                color = IslamicPrimary,
                fontWeight = FontWeight.Bold
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
