package id.ardion.quran.data.kemenag

import id.ardion.quran.data.response.KemenagCityDto
import id.ardion.quran.data.response.NominatimAddressDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KemenagCityMatcherTest {
    private val bogorCandidates = listOf(KemenagCityDto("1204", "KAB. BOGOR"), KemenagCityDto("1222", "KOTA BOGOR"))

    @Test
    fun countyMeansRegencyEvenWhenTownIsPresent() {
        val address = NominatimAddressDto(town = "Cibinong", county = "Kab Bogor", countryCode = "id")
        assertEquals(Regency(RegencyKind.REGENCY, "BOGOR"), address.toRegency())
    }

    @Test
    fun cityWithoutPrefixIsACity() {
        assertEquals(Regency(RegencyKind.CITY, "BOGOR"), NominatimAddressDto(city = "Bogor").toRegency())
        assertEquals(Regency(RegencyKind.CITY, "BANDUNG"), NominatimAddressDto(city = "Kota Bandung").toRegency())
    }

    @Test
    fun everyJakartaDistrictMapsToKotaJakarta() {
        val address = NominatimAddressDto(cityDistrict = "Jakarta Selatan", city = "Daerah Khusus Ibukota Jakarta")
        assertEquals(Regency(RegencyKind.CITY, "JAKARTA"), address.toRegency())
    }

    @Test
    fun addressWithoutRegionHasNoRegency() = assertNull(NominatimAddressDto(village = "Desa").toRegency())

    @Test
    fun kotaAndKabupatenWithSameNameAreDistinguished() {
        assertEquals("1222", matchKemenagCity(Regency(RegencyKind.CITY, "BOGOR"), bogorCandidates))
        assertEquals("1204", matchKemenagCity(Regency(RegencyKind.REGENCY, "BOGOR"), bogorCandidates))
    }

    @Test
    fun similarNamesDoNotMatch() {
        val candidates = listOf(KemenagCityDto("1201", "KAB. BANDUNG"), KemenagCityDto("1202", "KAB. BANDUNG BARAT"))
        assertEquals("1201", matchKemenagCity(Regency(RegencyKind.REGENCY, "BANDUNG"), candidates))
        assertNull(matchKemenagCity(Regency(RegencyKind.CITY, "BANDUNG TIMUR"), candidates))
    }

    @Test
    fun singleSameNameCandidateIsUsedWhenKindDiffers() {
        assertEquals("1301", matchKemenagCity(Regency(RegencyKind.REGENCY, "JAKARTA"), listOf(KemenagCityDto("1301", "KOTA JAKARTA"))))
    }

    @Test
    fun indonesiaIsDetectedFromCountryCode() {
        assertTrue(NominatimAddressDto(countryCode = "ID").isInIndonesia())
        assertFalse(NominatimAddressDto(countryCode = "sa").isInIndonesia())
        assertFalse(NominatimAddressDto().isInIndonesia())
    }
}
