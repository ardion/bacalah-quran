package id.ardion.quran.domain.irepository

import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.model.SurahItem

interface QuranRepository {
    suspend fun getAllSurahs(): List<SurahItem>
    suspend fun getSurahDetail(surahNumber: Int): SurahDetail
    suspend fun getPageDetail(pageNumber: Int): PageDetail
}
