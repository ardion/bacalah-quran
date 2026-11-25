package id.ardion.quran.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PageDataDto(
    @SerialName("number") val number: Int? = null,
    @SerialName("ayahs") val ayahs: List<AyahDto> = emptyList()
)

@Serializable
data class AyahDto(
    @SerialName("number") val number: Int? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("numberInSurah") val numberInSurah: Int? = null,
    @SerialName("juz") val juz: Int? = null,
    @SerialName("audio") val audio: String? = null,
    @SerialName("surah") val surah: SurahListItemDto? = null
)

@Serializable
data class SurahListItemDto(
    @SerialName("number") val number: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("englishName") val englishName: String? = null,
    @SerialName("englishNameTranslation") val englishNameTranslation: String? = null,
    @SerialName("numberOfAyahs") val numberOfAyahs: Int? = null,
    @SerialName("revelationType") val revelationType: String? = null
)

@Serializable
data class MultiEditionSurahDto(
    @SerialName("number") val number: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("englishName") val englishName: String? = null,
    @SerialName("numberOfAyahs") val numberOfAyahs: Int? = null,
    @SerialName("revelationType") val revelationType: String? = null,
    @SerialName("ayahs") val ayahs: List<AyahDto> = emptyList()
)
