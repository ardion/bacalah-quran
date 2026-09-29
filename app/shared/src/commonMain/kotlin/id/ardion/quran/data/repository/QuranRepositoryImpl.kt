package id.ardion.quran.data.repository

import id.ardion.quran.data.mapper.mapToSurahDetail
import id.ardion.quran.data.mapper.toDomain
import id.ardion.quran.data.remote.QuranApiService
import id.ardion.quran.domain.irepository.QuranRepository
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.model.SurahItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuranRepositoryImpl(
    private val apiService: QuranApiService
) : QuranRepository {

    override suspend fun getAllSurahs(): List<SurahItem> = withContext(Dispatchers.Default) {
        apiService.getAllSurahs().toDomain()
    }

    override suspend fun getSurahDetail(surahNumber: Int): SurahDetail = withContext(Dispatchers.Default) {
        val editions = apiService.getSurahDetailMultiEdition(surahNumber)
        mapToSurahDetail(editions)
    }
}
