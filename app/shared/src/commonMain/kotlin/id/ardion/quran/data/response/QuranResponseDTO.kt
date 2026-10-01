package id.ardion.quran.data.response

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

@Serializable
data class SurahDataDto(
    @SerialName("number") val number: Int,
    @SerialName("name") val name: String,
    @SerialName("englishName") val englishName: String,
    @SerialName("englishNameTranslation") val englishNameTranslation: String,
    @SerialName("numberOfAyahs") val numberOfAyahs: Int,
    @SerialName("revelationType") val revelationType: String,
    @SerialName("ayahs") val ayahs: List<AyahDto>
)

@Serializable
data class PageDataDto(
    @SerialName("number") val number: Int,
    @SerialName("ayahs") val ayahs: List<AyahDto>
)

@Serializable
data class AyahDto(
    @SerialName("number") val number: Int,
    @SerialName("text") val text: String,
    @SerialName("numberInSurah") val numberInSurah: Int,
    @SerialName("juz") val juz: Int,
    @SerialName("manzil") val manzil: Int,
    @SerialName("page") val page: Int,
    @SerialName("ruku") val ruku: Int,
    @SerialName("hizbQuarter") val hizbQuarter: Int,
    @SerialName("sajda") val sajda: JsonElement,
    @SerialName("audio") val audio: String? = null,
    @SerialName("surah") val surah: SurahListItemDto? = null
)

@Serializable
data class SurahListItemDto(
    @SerialName("number") val number: Int,
    @SerialName("name") val name: String,
    @SerialName("englishName") val englishName: String,
    @SerialName("englishNameTranslation") val englishNameTranslation: String,
    @SerialName("numberOfAyahs") val numberOfAyahs: Int,
    @SerialName("revelationType") val revelationType: String
)

@Serializable
data class MultiEditionSurahDto(
    @SerialName("number") val number: Int,
    @SerialName("name") val name: String,
    @SerialName("englishName") val englishName: String,
    @SerialName("numberOfAyahs") val numberOfAyahs: Int,
    @SerialName("revelationType") val revelationType: String,
    @SerialName("ayahs") val ayahs: List<AyahDto>
)
