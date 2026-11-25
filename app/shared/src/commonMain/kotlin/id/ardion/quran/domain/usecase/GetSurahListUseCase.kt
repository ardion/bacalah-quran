package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.SurahItem
import id.ardion.quran.domain.repository.QuranRepository

class GetSurahListUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(): Result<List<SurahItem>> = repository.getAllSurahs()
}
