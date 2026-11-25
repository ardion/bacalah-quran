package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.LastRead
import id.ardion.quran.domain.repository.LastReadRepository
import kotlinx.coroutines.flow.StateFlow

class GetLastReadUseCase(private val repository: LastReadRepository) {
    operator fun invoke(): StateFlow<LastRead> = repository.lastRead
}
