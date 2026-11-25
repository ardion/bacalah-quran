package id.ardion.quran.data.local

import id.ardion.quran.domain.model.MushafLastRead
import id.ardion.quran.domain.model.SurahLastRead
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val TOTAL_SURAH = 114
private const val TOTAL_MUSHAF_PAGE = 604

@Serializable
internal data class LastReadEntity(
    @SerialName("surahNumber") val surahNumber: Int? = null,
    @SerialName("surahName") val surahName: String? = null,
    @SerialName("pageNumber") val pageNumber: Int? = null,
    @SerialName("ayahNumber") val ayahNumber: Int? = null
)

internal fun LastReadEntity.toSurahLastRead(): SurahLastRead? {
    val surah = surahNumber?.takeIf { it in 1..TOTAL_SURAH } ?: return null
    return SurahLastRead(
        surahNumber = surah,
        surahName = surahName.orEmpty(),
        ayahNumber = ayahNumber?.takeIf { it > 0 } ?: 1
    )
}

internal fun LastReadEntity.toMushafLastRead(): MushafLastRead? {
    val page = pageNumber?.takeIf { it in 1..TOTAL_MUSHAF_PAGE } ?: return null
    return MushafLastRead(
        pageNumber = page,
        surahName = surahName?.takeIf { it.isNotBlank() },
        ayahNumber = ayahNumber?.takeIf { it > 0 }
    )
}

internal fun SurahLastRead.toEntity() =
    LastReadEntity(surahNumber = surahNumber, surahName = surahName, ayahNumber = ayahNumber)

internal fun MushafLastRead.toEntity() =
    LastReadEntity(pageNumber = pageNumber, surahName = surahName, ayahNumber = ayahNumber)
