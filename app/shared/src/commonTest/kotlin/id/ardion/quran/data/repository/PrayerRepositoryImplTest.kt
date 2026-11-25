package id.ardion.quran.data.repository

import id.ardion.quran.data.remote.KemenagApiService
import id.ardion.quran.data.remote.PrayerApiService
import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.PrayerTimeSource
import id.ardion.quran.fake.FakeResponseCache
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val NOMINATIM_BOGOR =
    """{"address":{"suburb":"Bogor Utara","city":"Bogor","state":"Jawa Barat","country_code":"id"}}"""
private const val NOMINATIM_MAKKAH =
    """{"address":{"suburb":"Al Haram","city":"Makkah","country_code":"sa"}}"""
private const val KEMENAG_SEARCH_BOGOR =
    """{"status":true,"data":[{"id":"1204","lokasi":"KAB. BOGOR"},{"id":"1222","lokasi":"KOTA BOGOR"}]}"""
private const val KEMENAG_SCHEDULE =
    """{"status":true,"data":{"lokasi":"KOTA BOGOR","jadwal":[{"date":"2026-11-25","subuh":"04:05","terbit":"05:24","dzuhur":"11:44","ashar":"15:09","maghrib":"17:57","isya":"19:11"}]}}"""
private const val ALADHAN =
    """{"data":{"timings":{"Fajr":"04:04","Sunrise":"05:25","Dhuhr":"11:43","Asr":"15:08","Maghrib":"17:56","Isha":"19:10"}}}"""

private fun kemenagMonth(month: Int, days: IntRange) =
    """{"status":true,"data":{"lokasi":"KOTA BOGOR","jadwal":[""" + days.joinToString(",") { day ->
        val date = "2026-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
        """{"date":"$date","subuh":"04:05","terbit":"05:19","dzuhur":"11:43","ashar":"15:07","maghrib":"18:01","isya":"19:11"}"""
    } + "]}}"

private fun aladhanCalendar(days: IntRange) =
    """{"data":[""" + days.joinToString(",") { day ->
        val date = "${day.toString().padStart(2, '0')}-11-2026"
        """{"timings":{"Fajr":"05:14 (AST)","Sunrise":"06:35 (AST)","Dhuhr":"12:12 (AST)","Asr":"15:12 (AST)","Maghrib":"17:41 (AST)","Isha":"19:11 (AST)"},"date":{"gregorian":{"date":"$date"}}}"""
    } + "]}"

class PrayerRepositoryImplTest {
    private val bogor = LocationCoordinates(-6.595, 106.8166)
    private val requests = mutableListOf<HttpRequestData>()

    private fun repository(
        cache: FakeResponseCache = FakeResponseCache(),
        respond: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): PrayerRepositoryImpl {
        val client = HttpClient(MockEngine { request -> requests += request; respond(request) }) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return PrayerRepositoryImpl(PrayerApiService(client), KemenagApiService(client), cache) { LocalDate(2026, 11, 25) }
    }

    private fun MockRequestHandleScope.json(body: String) =
        respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun MockRequestHandleScope.route(request: HttpRequestData, nominatim: String = NOMINATIM_BOGOR): HttpResponseData {
        val url = request.url.toString()
        return when {
            "nominatim" in url -> json(nominatim)
            "/kota/cari/" in url -> json(KEMENAG_SEARCH_BOGOR)
            "/jadwal/" in url -> json(KEMENAG_SCHEDULE)
            "aladhan" in url -> json(ALADHAN)
            else -> error("unexpected $url")
        }
    }

    @Test
    fun usesOfficialKemenagScheduleInIndonesia() = runTest {
        val times = repository { route(it) }.getPrayerTimes(bogor, isLiveLocation = true).getOrThrow()

        assertEquals(PrayerTimeSource.KEMENAG, times.source)
        assertEquals("17:57", times.maghrib)
        assertTrue(requests.any { "/jadwal/1222/2026/11" in it.url.toString() })
    }

    @Test
    fun fallsBackToTunedCalculationWhenKemenagFails() = runTest {
        val repository = repository { request ->
            if ("myquran" in request.url.toString()) error("myquran down") else route(request)
        }
        val times = repository.getPrayerTimes(bogor, isLiveLocation = true).getOrThrow()

        assertEquals(PrayerTimeSource.CALCULATED, times.source)
        val aladhan = requests.first { "aladhan" in it.url.toString() }
        assertEquals("20", aladhan.url.parameters["method"])
        assertEquals("3,3,-3,4,2,3,0,3,0", aladhan.url.parameters["tune"])
    }

    @Test
    fun outsideIndonesiaSkipsKemenag() = runTest {
        val times = repository { route(it, nominatim = NOMINATIM_MAKKAH) }
            .getPrayerTimes(LocationCoordinates(21.42, 39.82), isLiveLocation = true).getOrThrow()

        assertEquals(PrayerTimeSource.CALCULATED, times.source)
        assertTrue(requests.none { "myquran" in it.url.toString() })
    }

    @Test
    fun cachedPlaceAndScheduleWorkOffline() = runTest {
        val cache = FakeResponseCache()
        repository(cache) { route(it) }.getPrayerTimes(bogor, isLiveLocation = true)
        requests.clear()

        val offline = repository(cache) { error("offline") }
        val times = offline.getPrayerTimes(bogor, isLiveLocation = true).getOrThrow()

        assertEquals(PrayerTimeSource.KEMENAG, times.source)
        assertEquals("17:57", times.maghrib)
    }

    @Test
    fun offlineWithoutCacheFails() = runTest {
        val result = repository { error("offline") }.getPrayerTimes(bogor, isLiveLocation = true)
        assertTrue(result.isFailure)
    }

    private fun MockRequestHandleScope.scheduleRoute(request: HttpRequestData, nominatim: String = NOMINATIM_BOGOR): HttpResponseData {
        val url = request.url.toString()
        return when {
            "nominatim" in url -> json(nominatim)
            "/kota/cari/" in url -> json(KEMENAG_SEARCH_BOGOR)
            "/jadwal/1222/2026/11" in url -> json(kemenagMonth(11, 1..30))
            "/jadwal/1222/2026/12" in url -> json(kemenagMonth(12, 1..31))
            "/calendar/2026/11" in url -> json(aladhanCalendar(1..30))
            else -> error("unexpected $url")
        }
    }

    @Test
    fun scheduleSpanningMonthEndMergesTwoMonths() = runTest {
        val schedule = repository { scheduleRoute(it) }
            .getSchedule(bogor, isLiveLocation = true, from = LocalDate(2026, 11, 28), days = 5).getOrThrow()

        assertEquals(
            listOf(LocalDate(2026, 11, 28), LocalDate(2026, 11, 29), LocalDate(2026, 11, 30), LocalDate(2026, 12, 1), LocalDate(2026, 12, 2)),
            schedule.map { it.date }
        )
        assertTrue(schedule.all { it.source == PrayerTimeSource.KEMENAG })
        assertTrue(requests.any { "/jadwal/1222/2026/12" in it.url.toString() })
    }

    @Test
    fun scheduleFallsBackToAladhanCalendarOutsideIndonesia() = runTest {
        val schedule = repository { scheduleRoute(it, nominatim = NOMINATIM_MAKKAH) }
            .getSchedule(LocationCoordinates(21.42, 39.82), isLiveLocation = true, from = LocalDate(2026, 11, 25), days = 3)
            .getOrThrow()

        assertEquals(3, schedule.size)
        assertTrue(schedule.all { it.source == PrayerTimeSource.CALCULATED })
        val calendar = requests.first { "/calendar/2026/11" in it.url.toString() }
        assertEquals("3,3,-3,4,2,3,0,3,0", calendar.url.parameters["tune"])
    }

    @Test
    fun calendarTimesWithTimezoneSuffixAreNormalised() = runTest {
        val schedule = repository { scheduleRoute(it, nominatim = NOMINATIM_MAKKAH) }
            .getSchedule(LocationCoordinates(21.42, 39.82), isLiveLocation = true, from = LocalDate(2026, 11, 25), days = 1)
            .getOrThrow()

        assertEquals("05:14", schedule.single().fajr)
        assertEquals("17:41", schedule.single().maghrib)
    }

    @Test
    fun scheduleWorksOfflineFromCache() = runTest {
        val cache = FakeResponseCache()
        repository(cache) { scheduleRoute(it) }
            .getSchedule(bogor, isLiveLocation = true, from = LocalDate(2026, 11, 25), days = 3)

        val offline = repository(cache) { error("offline") }
            .getSchedule(bogor, isLiveLocation = true, from = LocalDate(2026, 11, 25), days = 3).getOrThrow()
        assertEquals(3, offline.size)
    }
}
