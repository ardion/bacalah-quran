package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.repository.QuranRepository

class GetSurahDetailUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(surahNumber: Int): Result<SurahDetail> = repository.getSurahDetail(surahNumber)
}
