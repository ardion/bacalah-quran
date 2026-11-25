package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.ReminderLocation
import id.ardion.quran.domain.repository.ReminderRepository

class UpdateReminderLocationUseCase(
    private val repository: ReminderRepository,
    private val scheduleReminders: ScheduleRemindersUseCase
) {
    suspend operator fun invoke(location: ReminderLocation) {
        repository.saveLastLocation(location)
        scheduleReminders()
    }
}
