package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.ReminderSettings
import id.ardion.quran.domain.repository.ReminderRepository

class SaveReminderSettingsUseCase(private val repository: ReminderRepository) {
    operator fun invoke(settings: ReminderSettings) = repository.saveSettings(settings)
}
