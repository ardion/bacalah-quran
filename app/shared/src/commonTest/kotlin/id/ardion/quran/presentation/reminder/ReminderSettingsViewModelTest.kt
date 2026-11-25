package id.ardion.quran.presentation.reminder

import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.Prayer
import id.ardion.quran.domain.model.PrayerTimeSource
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.domain.model.ReminderLocation
import id.ardion.quran.domain.model.ReminderMode
import id.ardion.quran.domain.model.ReminderSettings
import id.ardion.quran.domain.repository.PrayerRepository
import id.ardion.quran.domain.usecase.BuildReminderEventsUseCase
import id.ardion.quran.domain.usecase.FakeReminderRepository
import id.ardion.quran.domain.usecase.FakeReminderScheduler
import id.ardion.quran.domain.usecase.GetReminderSettingsUseCase
import id.ardion.quran.domain.usecase.SaveReminderSettingsUseCase
import id.ardion.quran.domain.usecase.ScheduleRemindersUseCase
import id.ardion.quran.domain.usecase.UpdateReminderLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class OneDaySchedule : PrayerRepository {
    override suspend fun getPrayerTimes(location: LocationCoordinates, isLiveLocation: Boolean): Result<PrayerTimes> =
        Result.failure(UnsupportedOperationException("not needed"))

    override suspend fun getSchedule(
        location: LocationCoordinates,
        isLiveLocation: Boolean,
        from: LocalDate,
        days: Int
    ): Result<List<PrayerTimes>> = Result.success(
        listOf(
            PrayerTimes(
                "04:05", "05:19", "11:43", "15:07", "18:01", "19:11", from,
                location.latitude, location.longitude, 295.0, "Bogor", isLiveLocation, PrayerTimeSource.KEMENAG
            )
        )
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderSettingsViewModelTest {
    private val repository = FakeReminderRepository(settings = ReminderSettings(isEnabled = false))
    private val scheduler = FakeReminderScheduler()
    private val scheduleReminders = ScheduleRemindersUseCase(
        repository, OneDaySchedule(), scheduler, BuildReminderEventsUseCase()
    ) { LocalDateTime(2026, 11, 25, 0, 0) }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(reminderScheduler: FakeReminderScheduler = scheduler) = ReminderSettingsViewModel(
        GetReminderSettingsUseCase(repository),
        SaveReminderSettingsUseCase(repository),
        scheduleReminders,
        reminderScheduler
    )

    @Test
    fun enablingSavesAndSchedules() {
        val viewModel = viewModel()
        viewModel.setEnabled(true)

        assertTrue(viewModel.state.value.settings.isEnabled)
        assertEquals(5, scheduler.scheduled.size)
    }

    @Test
    fun changingModeReschedules() {
        val viewModel = viewModel()
        viewModel.setEnabled(true)
        viewModel.setMode(Prayer.MAGHRIB, ReminderMode.ADZAN)

        assertEquals(ReminderMode.ADZAN, scheduler.scheduled.first { it.prayer == Prayer.MAGHRIB }.mode)
        assertEquals(ReminderMode.ADZAN, viewModel.state.value.settings.modeFor(Prayer.MAGHRIB))
    }

    @Test
    fun turningOffAPrayerRemovesItsReminder() {
        val viewModel = viewModel()
        viewModel.setEnabled(true)
        viewModel.setMode(Prayer.DHUHR, ReminderMode.OFF)

        assertTrue(scheduler.scheduled.none { it.prayer == Prayer.DHUHR })
    }

    @Test
    fun unsupportedPlatformReportsUnsupported() {
        assertFalse(viewModel(FakeReminderScheduler(isSupported = false)).state.value.isSupported)
    }

    @Test
    fun updatingLocationSavesItAndSchedules() = runTest {
        repository.saveSettings(ReminderSettings(isEnabled = true))
        val newLocation = ReminderLocation(LocationCoordinates(-7.25, 112.75), isLive = true)

        UpdateReminderLocationUseCase(repository, scheduleReminders)(newLocation)

        assertEquals(newLocation, repository.lastLocation())
        assertEquals(5, scheduler.scheduled.size)
    }
}
