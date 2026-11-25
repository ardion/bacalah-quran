package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.Prayer
import id.ardion.quran.domain.model.PrayerTimeSource
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.domain.model.ReminderMode
import id.ardion.quran.domain.model.ReminderSettings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildReminderEventsUseCaseTest {
    private val buildEvents = BuildReminderEventsUseCase()
    private val enabled = ReminderSettings(isEnabled = true)
    private val morning = LocalDateTime(2026, 11, 25, 0, 0)

    private fun day(date: LocalDate, fajr: String = "04:05") = PrayerTimes(
        fajr = fajr,
        sunrise = "05:19",
        dhuhr = "11:43",
        asr = "15:07",
        maghrib = "18:01",
        isha = "19:11",
        date = date,
        latitude = -6.595,
        longitude = 106.8166,
        qiblaBearing = 295.0,
        locationName = "Bogor Utara, Bogor",
        isLiveLocation = true,
        source = PrayerTimeSource.KEMENAG
    )

    private val today = day(LocalDate(2026, 11, 25))
    private val tomorrow = day(LocalDate(2026, 11, 26))

    @Test
    fun disabledSettingsProduceNothing() {
        assertTrue(buildEvents(listOf(today), ReminderSettings(isEnabled = false), morning, maxEvents = 64).isEmpty())
    }

    @Test
    fun offPrayersAreSkipped() {
        val settings = enabled.copy(modes = enabled.modes + (Prayer.DHUHR to ReminderMode.OFF))
        val events = buildEvents(listOf(today), settings, morning, maxEvents = 64)
        assertEquals(listOf(Prayer.FAJR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA), events.map { it.prayer })
    }

    @Test
    fun pastTimesAreSkipped() {
        val events = buildEvents(listOf(today), enabled, LocalDateTime(2026, 11, 25, 12, 0), maxEvents = 64)
        assertEquals(Prayer.ASR, events.first().prayer)
        assertEquals(LocalDateTime(2026, 11, 25, 15, 7), events.first().time)
    }

    @Test
    fun eventsAreChronologicalAndIdsSequential() {
        val events = buildEvents(listOf(tomorrow, today), enabled, morning, maxEvents = 64)
        assertEquals(10, events.size)
        assertEquals(events.sortedBy { it.time }, events)
        assertEquals((0 until 10).toList(), events.map { it.id })
    }

    @Test
    fun maxEventsIsRespected() {
        assertEquals(3, buildEvents(listOf(today, tomorrow), enabled, morning, maxEvents = 3).size)
    }

    @Test
    fun unparseableTimesAreSkipped() {
        val missing = buildEvents(listOf(day(LocalDate(2026, 11, 25), fajr = "--:--")), enabled, morning, maxEvents = 64)
        assertTrue(missing.none { it.prayer == Prayer.FAJR })

        val suffixed = buildEvents(listOf(day(LocalDate(2026, 11, 25), fajr = "04:03 (WIB)")), enabled, morning, maxEvents = 64)
        assertEquals(LocalDateTime(2026, 11, 25, 4, 3), suffixed.first().time)
    }

    @Test
    fun adzanModeAndLocationAreCarried() {
        val settings = enabled.copy(modes = enabled.modes + (Prayer.MAGHRIB to ReminderMode.ADZAN))
        val maghrib = buildEvents(listOf(today), settings, morning, maxEvents = 64).first { it.prayer == Prayer.MAGHRIB }
        assertEquals(ReminderMode.ADZAN, maghrib.mode)
        assertEquals("Bogor Utara, Bogor", maghrib.locationName)
    }
}
