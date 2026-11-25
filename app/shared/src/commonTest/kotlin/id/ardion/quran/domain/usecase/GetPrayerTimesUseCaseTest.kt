package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.LocationResult
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.domain.repository.LocationProvider
import id.ardion.quran.domain.repository.PrayerRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeLocationProvider(private val result: LocationResult) : LocationProvider {
    override suspend fun getCurrentLocation() = result
}

private class RecordingPrayerRepository : PrayerRepository {
    var requested: Pair<LocationCoordinates, Boolean>? = null
    override suspend fun getPrayerTimes(location: LocationCoordinates, isLiveLocation: Boolean): Result<PrayerTimes> {
        requested = location to isLiveLocation
        return Result.failure(UnsupportedOperationException("not needed"))
    }

    override suspend fun getSchedule(
        location: LocationCoordinates,
        isLiveLocation: Boolean,
        from: LocalDate,
        days: Int
    ): Result<List<PrayerTimes>> = Result.failure(UnsupportedOperationException("not needed"))
}

class GetPrayerTimesUseCaseTest {

    @Test
    fun usesDeviceLocationWhenAvailable() = runTest {
        val here = LocationCoordinates(-7.25, 112.75)
        val repository = RecordingPrayerRepository()
        GetPrayerTimesUseCase(FakeLocationProvider(LocationResult.Available(here)), repository)()
        assertEquals(here to true, repository.requested)
    }

    @Test
    fun fallsBackWhenPermissionDenied() = runTest {
        val repository = RecordingPrayerRepository()
        GetPrayerTimesUseCase(FakeLocationProvider(LocationResult.PermissionDenied), repository)()
        assertEquals(GetPrayerTimesUseCase.FALLBACK_LOCATION to false, repository.requested)
    }

    @Test
    fun fallsBackWhenLocationUnavailable() = runTest {
        val repository = RecordingPrayerRepository()
        GetPrayerTimesUseCase(FakeLocationProvider(LocationResult.Unavailable), repository)()
        assertEquals(GetPrayerTimesUseCase.FALLBACK_LOCATION to false, repository.requested)
    }
}
