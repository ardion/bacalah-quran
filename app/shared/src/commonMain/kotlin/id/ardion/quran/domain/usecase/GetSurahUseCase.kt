package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.irepository.QuranRepository
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.model.SurahItem

class GetSurahListUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(): Result<List<SurahItem>> = runCatching { repository.getAllSurahs() }
}

class GetSurahDetailUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(surahNumber: Int): Result<SurahDetail> = runCatching { repository.getSurahDetail(surahNumber) }
}
