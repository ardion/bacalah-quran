package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.SurahLastRead
import id.ardion.quran.domain.repository.LastReadRepository

class SaveSurahLastReadUseCase(private val repository: LastReadRepository) {
    operator fun invoke(position: SurahLastRead) = repository.saveSurah(position)
}
