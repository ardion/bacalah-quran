package id.ardion.quran.presentation.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ardion.quran.domain.model.Prayer
import id.ardion.quran.domain.model.ReminderMode
import id.ardion.quran.domain.model.ReminderSettings
import id.ardion.quran.domain.repository.ReminderScheduler
import id.ardion.quran.domain.usecase.GetReminderSettingsUseCase
import id.ardion.quran.domain.usecase.SaveReminderSettingsUseCase
import id.ardion.quran.domain.usecase.ScheduleRemindersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReminderUiState(
    val settings: ReminderSettings,
    val isSupported: Boolean,
    val canScheduleExact: Boolean
)

class ReminderSettingsViewModel(
    private val getSettings: GetReminderSettingsUseCase,
    private val saveSettings: SaveReminderSettingsUseCase,
    private val scheduleReminders: ScheduleRemindersUseCase,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    private val canScheduleExact = MutableStateFlow(scheduler.canScheduleExact)
    private var scheduleJob: Job? = null

    val state: StateFlow<ReminderUiState> = combine(getSettings(), canScheduleExact) { settings, exact ->
        ReminderUiState(settings, scheduler.isSupported, exact)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ReminderUiState(getSettings().value, scheduler.isSupported, scheduler.canScheduleExact)
    )

    fun setEnabled(enabled: Boolean) = update { it.copy(isEnabled = enabled) }

    fun setMode(prayer: Prayer, mode: ReminderMode) = update { it.copy(modes = it.modes + (prayer to mode)) }

    fun refreshExactAlarmStatus() {
        canScheduleExact.value = scheduler.canScheduleExact
    }

    fun openExactAlarmSettings() = scheduler.openExactAlarmSettings()

    private fun update(change: (ReminderSettings) -> ReminderSettings) {
        saveSettings(change(getSettings().value))
        scheduleJob?.cancel()
        scheduleJob = viewModelScope.launch { scheduleReminders() }
    }
}
