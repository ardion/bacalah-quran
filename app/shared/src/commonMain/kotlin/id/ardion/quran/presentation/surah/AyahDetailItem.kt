package id.ardion.quran.presentation.surah

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.presentation.components.TappableText
import id.ardion.quran.presentation.components.tajweedRuleAt
import id.ardion.quran.presentation.theme.IslamicCardHighlight
import id.ardion.quran.presentation.theme.IslamicGold
import id.ardion.quran.presentation.theme.IslamicPrimary
import id.ardion.quran.presentation.theme.IslamicTextPrimary
import id.ardion.quran.presentation.theme.IslamicTextSecondary
import id.ardion.quran.presentation.tajweed.TajweedInfo
import id.ardion.quran.presentation.tajweed.getTajweedInfo
import id.ardion.quran.presentation.tajweed.toAnnotatedString
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

@Composable
fun AyahDetailItem(
    ayah: AyahDetail,
    isPlaying: Boolean,
    isLastRead: Boolean,
    onPlayAudio: ((String) -> Unit)?,
    onBookmarkClick: () -> Unit,
    onTajweedClick: (TajweedInfo) -> Unit,
    isCurrentActive: Boolean = false
) {
    val isHighlighted = isPlaying || isLastRead || isCurrentActive
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) IslamicCardHighlight else Color.White
        ),
        border = if (isHighlighted) BorderStroke(if (isPlaying) 1.5.dp else 1.dp, IslamicGold) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AyahActionRow(
                ayah = ayah,
                isPlaying = isPlaying,
                isLastRead = isLastRead,
                onPlayAudio = onPlayAudio,
                onBookmarkClick = onBookmarkClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            val arabicText = remember(ayah.textTajweed) { ayah.textTajweed.toAnnotatedString() }
            TappableText(
                text = arabicText,
                style = LocalTextStyle.current.copy(
                    fontFamily = FontFamily(Font(Res.font.amiri)),
                    fontSize = 28.sp,
                    lineHeight = 56.sp,
                    textAlign = TextAlign.End,
                    color = IslamicTextPrimary,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                ),
                onTapOffset = { offset ->
                    arabicText.tajweedRuleAt(offset)?.let { onTajweedClick(getTajweedInfo(it)) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            AyahTranslation(ayah)
        }
    }
}

@Composable
private fun AyahActionRow(
    ayah: AyahDetail,
    isPlaying: Boolean,
    isLastRead: Boolean,
    onPlayAudio: ((String) -> Unit)?,
    onBookmarkClick: () -> Unit
) {
    val isHighlighted = isPlaying || isLastRead
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AyahNumberBadge(numberInSurah = ayah.numberInSurah, isHighlighted = isHighlighted, isLastRead = isLastRead)

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
                    contentDescription = stringResource(Res.string.last_read_tag),
                    tint = if (isLastRead) IslamicGold else IslamicPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val playAudio = onPlayAudio
            val audioUrl = ayah.audioUrl
            if (playAudio != null && audioUrl != null) {
                IconButton(
                    onClick = { playAudio(audioUrl) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) IslamicPrimary else IslamicPrimary.copy(alpha = 0.1f))
            ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(if (isPlaying) Res.string.pause_ayah else Res.string.play_ayah),
                        tint = if (isPlaying) Color.White else IslamicPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AyahNumberBadge(numberInSurah: Int, isHighlighted: Boolean, isLastRead: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isHighlighted) IslamicGold else IslamicPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$numberInSurah",
                color = if (isHighlighted) Color.White else IslamicPrimary,
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
                    text = stringResource(Res.string.last_read_tag),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AyahTranslation(ayah: AyahDetail) {
    ayah.transliteration?.takeIf { it.isNotEmpty() }?.let { transliteration ->
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = transliteration,
            style = MaterialTheme.typography.bodyMedium,
            color = IslamicGold,
            fontWeight = FontWeight.Medium,
            lineHeight = 22.sp
        )
    }

    if (ayah.translation.isNotEmpty()) {
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(Res.string.translation_label),
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
