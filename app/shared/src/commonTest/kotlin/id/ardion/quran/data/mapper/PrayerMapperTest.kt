package id.ardion.quran.data.mapper

import id.ardion.quran.data.response.AladhanDataDto
import id.ardion.quran.data.response.GeocodeResponseDto
import id.ardion.quran.data.response.NominatimAddressDto
import id.ardion.quran.data.response.TimingsDto
import id.ardion.quran.domain.model.LocationCoordinates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PrayerMapperTest {
    private val jakarta = LocationCoordinates(-6.2, 106.8)

    @Test
    fun missingTimingsMapToNull() {
        assertNull(AladhanDataDto().toDomain(jakarta, 295.0, null, isLiveLocation = true))
    }

    @Test
    fun missingSingleTimeUsesPlaceholder() {
        val times = AladhanDataDto(timings = TimingsDto(fajr = "04:30"))
            .toDomain(jakarta, 295.0, "Menteng, Jakarta", isLiveLocation = false)
        assertEquals("04:30", assertNotNull(times).fajr)
        assertEquals(MISSING_TIME, times.isha)
        assertEquals(false, times.isLiveLocation)
    }

    @Test
    fun nominatimStripsAdministrativePrefixes() {
        val name = NominatimAddressDto(suburb = "Kelurahan Menteng", city = "Kota Jakarta Pusat").formatLocationName()
        assertEquals("Menteng, Jakarta Pusat", name)
    }

    @Test
    fun nominatimWithoutKelurahanIsNull() {
        assertNull(NominatimAddressDto(city = "Bandung").formatLocationName())
    }

    @Test
    fun nominatimDoesNotRepeatSameName() {
        assertEquals("Bogor", NominatimAddressDto(village = "Bogor", town = "Bogor").formatLocationName())
    }

    @Test
    fun bigDataNeedsLocalityAndCity() {
        assertNull(GeocodeResponseDto(locality = "Kecamatan Coblong").formatLocationName())
        assertEquals("Coblong, Bandung", GeocodeResponseDto("Kecamatan Coblong", "Kota Bandung").formatLocationName())
    }
}
