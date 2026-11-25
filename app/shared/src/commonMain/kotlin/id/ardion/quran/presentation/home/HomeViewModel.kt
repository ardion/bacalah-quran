package id.ardion.quran.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ardion.quran.domain.model.LastRead
import id.ardion.quran.domain.usecase.GetLastReadUseCase
import id.ardion.quran.domain.usecase.ScheduleRemindersUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    getLastRead: GetLastReadUseCase,
    scheduleReminders: ScheduleRemindersUseCase
) : ViewModel() {
    val lastRead: StateFlow<LastRead> = getLastRead()

    init {
        viewModelScope.launch { scheduleReminders() }
    }
}
