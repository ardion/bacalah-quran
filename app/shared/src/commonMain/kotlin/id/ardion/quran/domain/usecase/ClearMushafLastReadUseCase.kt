package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.repository.LastReadRepository

class ClearMushafLastReadUseCase(private val repository: LastReadRepository) {
    operator fun invoke() = repository.clearMushaf()
}
