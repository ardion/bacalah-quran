package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.irepository.QuranRepository
import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.model.SurahItem

class GetSurahListUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(): List<SurahItem> = repository.getAllSurahs()
}

class GetSurahDetailUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(surahNumber: Int): SurahDetail = repository.getSurahDetail(surahNumber)
}

class GetPageDetailUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(pageNumber: Int): PageDetail = repository.getPageDetail(pageNumber)
}
