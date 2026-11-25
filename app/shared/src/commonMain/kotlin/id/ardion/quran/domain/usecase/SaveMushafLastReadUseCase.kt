package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.MushafLastRead
import id.ardion.quran.domain.repository.LastReadRepository

class SaveMushafLastReadUseCase(private val repository: LastReadRepository) {
    operator fun invoke(position: MushafLastRead) = repository.saveMushaf(position)
}
