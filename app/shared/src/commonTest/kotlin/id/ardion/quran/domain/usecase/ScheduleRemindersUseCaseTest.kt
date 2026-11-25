package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.PrayerTimeSource
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.domain.model.ReminderEvent
import id.ardion.quran.domain.model.ReminderLocation
import id.ardion.quran.domain.model.ReminderSettings
import id.ardion.quran.domain.repository.PrayerRepository
import id.ardion.quran.domain.repository.ReminderRepository
import id.ardion.quran.domain.repository.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeReminderScheduler(
    override val isSupported: Boolean = true,
    override val maxEvents: Int = 64,
    override val windowDays: Int = 7
) : ReminderScheduler {
    override val canScheduleExact = true
    var scheduled: List<ReminderEvent> = emptyList()
    var cancelCount = 0

    override suspend fun schedule(events: List<ReminderEvent>) {
        scheduled = events
    }

    override fun cancelAll() {
        cancelCount++
        scheduled = emptyList()
    }

    override fun openExactAlarmSettings() = Unit
}

class FakeReminderRepository(
    settings: ReminderSettings = ReminderSettings(isEnabled = true),
    private var location: ReminderLocation? = ReminderLocation(LocationCoordinates(-6.595, 106.8166), isLive = true)
) : ReminderRepository {
    private val state = MutableStateFlow(settings)
    override val settings: StateFlow<ReminderSettings> = state
    override fun saveSettings(settings: ReminderSettings) { state.value = settings }
    override fun lastLocation(): ReminderLocation? = location
    override fun saveLastLocation(location: ReminderLocation) { this.location = location }
}

private class ScheduleSource(var result: Result<List<PrayerTimes>>) : PrayerRepository {
    var requestedDays: Int? = null

    override suspend fun getPrayerTimes(location: LocationCoordinates, isLiveLocation: Boolean): Result<PrayerTimes> =
        Result.failure(UnsupportedOperationException("not needed"))

    override suspend fun getSchedule(
        location: LocationCoordinates,
        isLiveLocation: Boolean,
        from: LocalDate,
        days: Int
    ): Result<List<PrayerTimes>> {
        requestedDays = days
        return result
    }
}

private fun dayOf(date: LocalDate) = PrayerTimes(
    "04:05", "05:19", "11:43", "15:07", "18:01", "19:11", date,
    -6.595, 106.8166, 295.0, "Bogor", true, PrayerTimeSource.KEMENAG
)

class ScheduleRemindersUseCaseTest {
    private val now = LocalDateTime(2026, 11, 25, 12, 0)
    private val twoDays = listOf(dayOf(LocalDate(2026, 11, 25)), dayOf(LocalDate(2026, 11, 26)))

    private fun useCase(
        repository: ReminderRepository,
        source: PrayerRepository,
        scheduler: ReminderScheduler
    ) = ScheduleRemindersUseCase(repository, source, scheduler, BuildReminderEventsUseCase()) { now }

    @Test
    fun disabledCancelsEverything() = runTest {
        val scheduler = FakeReminderScheduler()
        useCase(FakeReminderRepository(ReminderSettings(isEnabled = false)), ScheduleSource(Result.success(twoDays)), scheduler)()
        assertEquals(1, scheduler.cancelCount)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun missingLocationCancelsEverything() = runTest {
        val scheduler = FakeReminderScheduler()
        useCase(FakeReminderRepository(location = null), ScheduleSource(Result.success(twoDays)), scheduler)()
        assertEquals(1, scheduler.cancelCount)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun enabledSchedulesEventsWithinWindow() = runTest {
        val scheduler = FakeReminderScheduler(windowDays = 7)
        val source = ScheduleSource(Result.success(twoDays))
        useCase(FakeReminderRepository(), source, scheduler)()

        assertEquals(7, source.requestedDays)
        assertEquals(8, scheduler.scheduled.size)
        assertTrue(scheduler.scheduled.all { it.time > now })
    }

    @Test
    fun scheduleFailureKeepsExistingReminders() = runTest {
        val scheduler = FakeReminderScheduler()
        val repository = FakeReminderRepository()
        val source = ScheduleSource(Result.success(twoDays))
        useCase(repository, source, scheduler)()
        val before = scheduler.scheduled

        source.result = Result.failure(IllegalStateException("offline"))
        useCase(repository, source, scheduler)()
        assertEquals(before, scheduler.scheduled)
    }

    @Test
    fun unsupportedPlatformDoesNothing() = runTest {
        val scheduler = FakeReminderScheduler(isSupported = false)
        useCase(FakeReminderRepository(), ScheduleSource(Result.success(twoDays)), scheduler)()
        assertEquals(0, scheduler.cancelCount)
        assertTrue(scheduler.scheduled.isEmpty())
    }
}
