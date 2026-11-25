package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.ReminderSettings
import id.ardion.quran.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.StateFlow

class GetReminderSettingsUseCase(private val repository: ReminderRepository) {
    operator fun invoke(): StateFlow<ReminderSettings> = repository.settings
}
