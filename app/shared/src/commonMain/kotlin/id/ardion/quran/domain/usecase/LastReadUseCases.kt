package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.irepository.LastReadRepository
import id.ardion.quran.domain.model.MultiLastReadInfo
import kotlinx.coroutines.flow.StateFlow

class GetLastReadUseCase(
    private val repository: LastReadRepository
) {
    operator fun invoke(): StateFlow<MultiLastReadInfo> = repository.multiLastRead
}

class SaveLastReadSurahUseCase(
    private val repository: LastReadRepository
) {
    operator fun invoke(surahNumber: Int, surahName: String, ayahNumber: Int) {
        repository.saveLastReadSurah(surahNumber, surahName, ayahNumber)
    }
}

class SaveLastReadPageUseCase(
    private val repository: LastReadRepository
) {
    operator fun invoke(pageNumber: Int, surahName: String = "", ayahNumber: Int = 1) {
        repository.saveLastReadPage(pageNumber, surahName, ayahNumber)
    }
}

class ClearLastReadUseCase(
    private val repository: LastReadRepository
) {
    fun clearSurah() = repository.clearSurahLastRead()
    fun clearMushaf() = repository.clearMushafLastRead()
}
