package id.ardion.quran.data.kemenag

import id.ardion.quran.data.response.KemenagCityDto
import id.ardion.quran.data.response.NominatimAddressDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val JAKARTA = "JAKARTA"
private const val INDONESIA_COUNTRY_CODE = "id"
private const val CITY_PREFIX = "KOTA "
private const val REGENCY_PREFIX = "KAB"
private val WHITESPACE = Regex("\\s+")

private val administrativePrefixes = listOf(
    "DAERAH KHUSUS IBUKOTA ",
    "KOTA ADMINISTRASI ",
    "KABUPATEN ",
    "KAB. ",
    "KAB ",
    "KOTA "
)

@Serializable
enum class RegencyKind { CITY, REGENCY }

@Serializable
data class Regency(
    @SerialName("kind") val kind: RegencyKind,
    @SerialName("name") val name: String
) {
    val searchKeyword: String get() = name.lowercase()
}

internal fun NominatimAddressDto.isInIndonesia(): Boolean =
    countryCode.equals(INDONESIA_COUNTRY_CODE, ignoreCase = true)

internal fun NominatimAddressDto.toRegency(): Regency? {
    val cityName = city?.let(::normalizeRegionName)?.takeIf { it.isNotEmpty() }
    val countyName = county?.let(::normalizeRegionName)?.takeIf { it.isNotEmpty() }
    return when {
        cityName?.contains(JAKARTA) == true -> Regency(RegencyKind.CITY, JAKARTA)
        countyName != null -> Regency(RegencyKind.REGENCY, countyName)
        cityName != null -> Regency(RegencyKind.CITY, cityName)
        else -> null
    }
}

internal fun matchKemenagCity(regency: Regency, candidates: List<KemenagCityDto>): String? {
    val sameName = candidates.filter { candidate ->
        candidate.name?.let(::normalizeRegionName) == regency.name
    }
    val sameKind = sameName.firstOrNull { it.name?.let(::kindOf) == regency.kind }
    return (sameKind ?: sameName.singleOrNull())?.id
}

private fun kindOf(kemenagName: String): RegencyKind? {
    val upper = kemenagName.uppercase()
    return when {
        upper.startsWith(CITY_PREFIX) -> RegencyKind.CITY
        upper.startsWith(REGENCY_PREFIX) -> RegencyKind.REGENCY
        else -> null
    }
}

internal fun normalizeRegionName(raw: String): String {
    var name = raw.uppercase().trim().replace(WHITESPACE, " ")
    var stripped = true
    while (stripped) {
        stripped = false
        administrativePrefixes.firstOrNull { name.startsWith(it) }?.let {
            name = name.removePrefix(it).trim()
            stripped = true
        }
    }
    return name
}
