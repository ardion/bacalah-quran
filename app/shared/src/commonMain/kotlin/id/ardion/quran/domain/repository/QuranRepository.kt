package id.ardion.quran.domain.repository

import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.model.SurahItem

interface QuranRepository {
    suspend fun getAllSurahs(): Result<List<SurahItem>>
    suspend fun getSurahDetail(surahNumber: Int): Result<SurahDetail>
    suspend fun getPageDetail(pageNumber: Int): Result<PageDetail>
}
