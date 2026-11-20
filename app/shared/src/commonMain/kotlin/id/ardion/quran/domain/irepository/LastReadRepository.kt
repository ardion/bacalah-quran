package id.ardion.quran.domain.irepository

import id.ardion.quran.domain.model.MultiLastReadInfo
import kotlinx.coroutines.flow.StateFlow

interface LastReadRepository {
    val multiLastRead: StateFlow<MultiLastReadInfo>
    fun saveLastReadSurah(surahNumber: Int, surahName: String, ayahNumber: Int)
    fun saveLastReadPage(pageNumber: Int, surahName: String = "", ayahNumber: Int = 1)
    fun clearSurahLastRead()
    fun clearMushafLastRead()
}
