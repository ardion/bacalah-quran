package id.ardion.quran.data.repository

import id.ardion.quran.data.kemenag.CachedPlace
import id.ardion.quran.data.kemenag.Regency
import id.ardion.quran.data.kemenag.isInIndonesia
import id.ardion.quran.data.kemenag.matchKemenagCity
import id.ardion.quran.data.kemenag.toRegency
import id.ardion.quran.data.local.ResponseCache
import id.ardion.quran.data.mapper.PrayerContext
import id.ardion.quran.data.mapper.formatLocationName
import id.ardion.quran.data.mapper.toDomain
import id.ardion.quran.data.remote.KemenagApiService
import id.ardion.quran.data.remote.PrayerApiService
import id.ardion.quran.data.response.AladhanCalendarResponseDto
import id.ardion.quran.data.response.KemenagScheduleDto
import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.domain.qibla.QiblaCalculator
import id.ardion.quran.domain.repository.PrayerRepository
import id.ardion.quran.domain.time.TodayProvider
import id.ardion.quran.exception.NetworkException
import id.ardion.quran.logging.logWarning
import id.ardion.quran.result.runCatchingCancellable
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.number
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.math.roundToLong

private const val TAG = "PrayerRepository"
private const val PLACE_GRID_SCALE = 100.0
private val UNSAFE_KEY_CHARS = Regex("[^A-Za-z0-9_.-]")

class PrayerRepositoryImpl(
    private val prayerApi: PrayerApiService,
    private val kemenagApi: KemenagApiService,
    private val cache: ResponseCache,
    private val todayProvider: TodayProvider
) : PrayerRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getPrayerTimes(
        location: LocationCoordinates,
        isLiveLocation: Boolean
    ): Result<PrayerTimes> {
        val place = resolvePlace(location)
        val context = PrayerContext(
            location = location,
            qiblaBearing = QiblaCalculator.calculateQiblaBearing(location.latitude, location.longitude),
            locationName = place.locationName,
            isLiveLocation = isLiveLocation,
            date = todayProvider.today()
        )

        val official = place.regency
            ?.takeIf { place.isInIndonesia }
            ?.let { kemenagTimes(it, context) }
        return official?.let { Result.success(it) } ?: calculatedTimes(context)
    }

    override suspend fun getSchedule(
        location: LocationCoordinates,
        isLiveLocation: Boolean,
        from: LocalDate,
        days: Int
    ): Result<List<PrayerTimes>> {
        val place = resolvePlace(location)
        val baseContext = PrayerContext(
            location = location,
            qiblaBearing = QiblaCalculator.calculateQiblaBearing(location.latitude, location.longitude),
            locationName = place.locationName,
            isLiveLocation = isLiveLocation,
            date = from
        )
        val cityId = place.regency?.takeIf { place.isInIndonesia }?.let { kemenagCityId(it) }
        val dates = (0 until days).map { from.plus(it, DateTimeUnit.DAY) }

        val byDate = dates.map { it.year to it.month.number }
            .distinct()
            .flatMap { (year, month) -> monthTimes(cityId, LocalDate(year, month, 1), baseContext) }
            .associateBy { it.date }
        val schedule = dates.mapNotNull { byDate[it] }
        return if (schedule.isEmpty()) Result.failure(NetworkException.EmptyResponse) else Result.success(schedule)
    }

    private suspend fun monthTimes(cityId: String?, firstDay: LocalDate, base: PrayerContext): List<PrayerTimes> {
        val official = cityId?.let { monthlySchedule(it, firstDay) }?.days?.mapNotNull { day ->
            day.date?.let(::parseIsoDate)?.let { date -> day.toDomain(base.copy(date = date)) }
        }
        if (!official.isNullOrEmpty()) return official

        return calendarMonth(base.location, firstDay)?.data.orEmpty().mapNotNull { day ->
            val date = day.date?.gregorian?.date?.let(::parseDayMonthYear) ?: return@mapNotNull null
            day.timings?.toDomain(base.copy(date = date))
        }
    }

    private suspend fun calendarMonth(location: LocationCoordinates, firstDay: LocalDate): AladhanCalendarResponseDto? {
        val key = cacheKey(
            "aladhan_calendar",
            gridCoordinate(location.latitude),
            gridCoordinate(location.longitude),
            firstDay.year.toString(),
            firstDay.month.number.toString()
        )
        readCached(key, AladhanCalendarResponseDto.serializer())?.let { return it }

        val calendar = runCatchingCancellable { prayerApi.fetchPrayerCalendar(location, firstDay.year, firstDay.month.number) }
            .onFailure { logWarning(TAG, "Aladhan calendar failed for ${firstDay.year}-${firstDay.month.number}", it) }
            .getOrNull()
            ?.takeIf { it.data.isNotEmpty() }
            ?: return null
        cache.write(key, json.encodeToString(AladhanCalendarResponseDto.serializer(), calendar))
        return calendar
    }

    private suspend fun calculatedTimes(context: PrayerContext): Result<PrayerTimes> {
        val response = runCatchingCancellable { prayerApi.fetchPrayerTimings(context.location) }
            .getOrElse { return Result.failure(it) }
        return response.data?.toDomain(context)?.let { Result.success(it) }
            ?: Result.failure(NetworkException.EmptyResponse)
    }

    private suspend fun kemenagTimes(regency: Regency, context: PrayerContext): PrayerTimes? {
        val cityId = kemenagCityId(regency) ?: return null
        val schedule = monthlySchedule(cityId, context.date) ?: return null
        return schedule.days.firstOrNull { it.date == context.date.toString() }?.toDomain(context)
    }

    private suspend fun kemenagCityId(regency: Regency): String? {
        val key = cacheKey("kemenag_city", regency.kind.name, regency.name)
        cache.read(key)?.let { return it }

        val cityId = runCatchingCancellable { kemenagApi.searchCity(regency.searchKeyword) }
            .onFailure { logWarning(TAG, "Kemenag city search failed for ${regency.name}", it) }
            .getOrNull()
            ?.let { matchKemenagCity(regency, it) }
            ?: return null
        cache.write(key, cityId)
        return cityId
    }

    private suspend fun monthlySchedule(cityId: String, date: LocalDate): KemenagScheduleDto? {
        val key = cacheKey("kemenag_schedule", cityId, date.year.toString(), date.month.number.toString())
        readCached(key, KemenagScheduleDto.serializer())?.let { return it }

        val schedule = runCatchingCancellable { kemenagApi.monthlySchedule(cityId, date.year, date.month.number) }
            .onFailure { logWarning(TAG, "Kemenag schedule failed for city $cityId", it) }
            .getOrNull()
            ?.takeIf { it.days.isNotEmpty() }
            ?: return null
        cache.write(key, json.encodeToString(KemenagScheduleDto.serializer(), schedule))
        return schedule
    }

    private suspend fun resolvePlace(location: LocationCoordinates): CachedPlace {
        val key = cacheKey("place", gridCoordinate(location.latitude), gridCoordinate(location.longitude))
        val address = runCatchingCancellable { prayerApi.fetchReverseGeocodeNominatim(location).address }
            .onFailure { logWarning(TAG, "Nominatim reverse geocoding failed", it) }
            .getOrNull()

        if (address != null) {
            val place = CachedPlace(
                regency = address.toRegency(),
                locationName = address.formatLocationName() ?: fallbackLocationName(location),
                isInIndonesia = address.isInIndonesia()
            )
            cache.write(key, json.encodeToString(CachedPlace.serializer(), place))
            return place
        }
        return readCached(key, CachedPlace.serializer()) ?: CachedPlace(locationName = fallbackLocationName(location))
    }

    private suspend fun fallbackLocationName(location: LocationCoordinates): String? =
        runCatchingCancellable { prayerApi.fetchReverseGeocodeBigData(location).formatLocationName() }
            .onFailure { logWarning(TAG, "BigDataCloud reverse geocoding failed", it) }
            .getOrNull()

    private fun <T> readCached(key: String, serializer: KSerializer<T>): T? {
        val raw = cache.read(key) ?: return null
        return try {
            json.decodeFromString(serializer, raw)
        } catch (e: IllegalArgumentException) {
            logWarning(TAG, "Dropping unreadable cache entry $key", e)
            cache.remove(key)
            null
        }
    }

    private fun cacheKey(vararg parts: String): String =
        parts.joinToString("_") { it.replace(UNSAFE_KEY_CHARS, "_") }

    private fun gridCoordinate(value: Double): String =
        ((value * PLACE_GRID_SCALE).roundToLong() / PLACE_GRID_SCALE).toString()
}

private const val DATE_SEPARATOR = '-'

private fun parseIsoDate(raw: String): LocalDate? {
    val parts = raw.split(DATE_SEPARATOR).mapNotNull { it.toIntOrNull() }
    return parts.takeIf { it.size == 3 }?.let { (year, month, day) -> safeDate(year, month, day) }
}

private fun parseDayMonthYear(raw: String): LocalDate? {
    val parts = raw.split(DATE_SEPARATOR).mapNotNull { it.toIntOrNull() }
    return parts.takeIf { it.size == 3 }?.let { (day, month, year) -> safeDate(year, month, day) }
}

private fun safeDate(year: Int, month: Int, day: Int): LocalDate? =
    try {
        LocalDate(year, month, day)
    } catch (_: IllegalArgumentException) {
        null
    }
