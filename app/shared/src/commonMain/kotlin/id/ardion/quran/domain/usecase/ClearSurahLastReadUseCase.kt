package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.repository.LastReadRepository

class ClearSurahLastReadUseCase(private val repository: LastReadRepository) {
    operator fun invoke() = repository.clearSurah()
}
