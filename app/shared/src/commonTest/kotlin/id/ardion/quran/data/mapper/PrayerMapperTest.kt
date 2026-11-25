package id.ardion.quran.data.mapper

import id.ardion.quran.data.response.AladhanDataDto
import id.ardion.quran.data.response.GeocodeResponseDto
import id.ardion.quran.data.response.KemenagDayDto
import id.ardion.quran.data.response.NominatimAddressDto
import id.ardion.quran.data.response.TimingsDto
import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.PrayerTimeSource
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PrayerMapperTest {
    private val context = PrayerContext(
        location = LocationCoordinates(-6.2, 106.8),
        qiblaBearing = 295.0,
        locationName = "Menteng, Jakarta",
        isLiveLocation = true,
        date = LocalDate(2026, 11, 25)
    )

    @Test
    fun missingTimingsMapToNull() = assertNull(AladhanDataDto().toDomain(context))

    @Test
    fun aladhanIsMarkedAsCalculated() {
        val times = assertNotNull(AladhanDataDto(timings = TimingsDto(fajr = "04:30")).toDomain(context))
        assertEquals("04:30", times.fajr)
        assertEquals(MISSING_TIME, times.isha)
        assertEquals(PrayerTimeSource.CALCULATED, times.source)
        assertEquals(LocalDate(2026, 11, 25), times.date)
    }

    @Test
    fun kemenagDayIsMarkedAsOfficial() {
        val day = KemenagDayDto("2026-11-25", "04:06", "05:23", "11:43", "15:06", "17:56", "19:10")
        val times = assertNotNull(day.toDomain(context))
        assertEquals("17:56", times.maghrib)
        assertEquals(PrayerTimeSource.KEMENAG, times.source)
    }

    @Test
    fun incompleteKemenagDayIsRejected() =
        assertNull(KemenagDayDto(date = "2026-11-25", fajr = "04:06").toDomain(context))

    @Test
    fun nominatimStripsAdministrativePrefixes() {
        val name = NominatimAddressDto(suburb = "Kelurahan Menteng", city = "Kota Jakarta Pusat").formatLocationName()
        assertEquals("Menteng, Jakarta Pusat", name)
    }

    @Test
    fun nominatimWithoutKelurahanIsNull() = assertNull(NominatimAddressDto(city = "Bandung").formatLocationName())

    @Test
    fun nominatimDoesNotRepeatSameName() =
        assertEquals("Bogor", NominatimAddressDto(village = "Bogor", town = "Bogor").formatLocationName())

    @Test
    fun bigDataNeedsLocalityAndCity() {
        assertNull(GeocodeResponseDto(locality = "Kecamatan Coblong").formatLocationName())
        assertEquals("Coblong, Bandung", GeocodeResponseDto("Kecamatan Coblong", "Kota Bandung").formatLocationName())
    }
}
