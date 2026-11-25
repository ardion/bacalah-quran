package id.ardion.quran.presentation.mushaf

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.domain.model.MushafLastRead
import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.presentation.components.TappableText
import id.ardion.quran.presentation.components.tajweedRuleAt
import id.ardion.quran.presentation.theme.IslamicGold
import id.ardion.quran.presentation.theme.IslamicGoldDark
import id.ardion.quran.presentation.theme.IslamicPrimary
import id.ardion.quran.presentation.theme.IslamicTextPrimary
import id.ardion.quran.presentation.tajweed.TajweedInfo
import id.ardion.quran.presentation.tajweed.getTajweedInfo
import id.ardion.quran.presentation.tajweed.toAnnotatedString
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

private const val AYAH_MARKER_TAG = "AYAH_MARKER"
private val MushafHeaderBackground = Color(0xFFEAF5ED)

@Composable
fun MushafPageContent(
    pageDetail: PageDetail,
    bookmark: MushafLastRead?,
    onBookmarkToggle: (AyahDetail) -> Unit,
    onTajweedClick: (TajweedInfo) -> Unit
) {
    val headerAyah = pageDetail.ayahs.firstOrNull { it.numberInSurah == 1 } ?: pageDetail.ayahs.firstOrNull()
    val surahName = headerAyah?.surahName
    val revelationType = headerAyah?.revelationType

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            MushafSurahHeader(
                surahName = surahName,
                revelationType = revelationType,
                ayahCount = pageDetail.ayahs.size
            )
        }

        item {
            val pageText = remember(pageDetail, bookmark) { buildPageText(pageDetail, bookmark) }
            TappableText(
                text = pageText,
                style = TextStyle(
                    fontFamily = FontFamily(Font(Res.font.amiri)),
                    fontSize = 26.sp,
                    lineHeight = 56.sp,
                    color = IslamicTextPrimary
                ),
                onTapOffset = { offset ->
                    val marker = pageText.getStringAnnotations(AYAH_MARKER_TAG, offset, offset).firstOrNull()
                    if (marker != null) {
                        marker.item.toIntOrNull()?.let(pageDetail.ayahs::getOrNull)?.let(onBookmarkToggle)
                    } else {
                        pageText.tajweedRuleAt(offset)?.let { onTajweedClick(getTajweedInfo(it)) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun MushafSurahHeader(surahName: String?, revelationType: String?, ayahCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MushafHeaderBackground),
        border = BorderStroke(1.dp, IslamicPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            revelationType?.let { HeaderBadge(it, IslamicPrimary.copy(alpha = 0.12f), IslamicPrimary) }
            surahName?.let {
                Text(
                    text = stringResource(Res.string.surah_prefix, it),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicPrimary
                )
            }
            HeaderBadge(
                stringResource(Res.string.ayah_count_format, ayahCount),
                IslamicGold.copy(alpha = 0.15f),
                IslamicGoldDark
            )
        }
    }
}

@Composable
private fun HeaderBadge(text: String, containerColor: Color, contentColor: Color) {
    Badge(containerColor = containerColor, contentColor = contentColor) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun buildPageText(pageDetail: PageDetail, bookmark: MushafLastRead?): AnnotatedString = buildAnnotatedString {
    pushStyle(ParagraphStyle(textAlign = TextAlign.Center))

    pageDetail.ayahs.forEachIndexed { index, ayah ->
        append(ayah.textTajweed.toAnnotatedString())
        append(" ")

        val isBookmarked = bookmark?.matches(pageDetail.pageNumber, ayah) == true
        val markerStart = length
        val markerText = "\u200F﴿${toArabicNumbers(ayah.numberInSurah)}﴾\u200F"

        pushStyle(
            SpanStyle(
                color = if (isBookmarked) Color.White else IslamicGold,
                background = if (isBookmarked) IslamicGold else Color.Transparent,
                fontWeight = FontWeight.Bold
            )
        )
        append(markerText)
        pop()

        addStringAnnotation(AYAH_MARKER_TAG, "$index", markerStart, markerStart + markerText.length)
        append(" ")
    }
    pop()
}

internal fun MushafLastRead.matches(page: Int, ayah: AyahDetail): Boolean =
    pageNumber == page && ayahNumber == ayah.numberInSurah && (surahName == null || surahName == ayah.surahName)
