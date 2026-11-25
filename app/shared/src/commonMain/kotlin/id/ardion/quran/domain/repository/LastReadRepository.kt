package id.ardion.quran.domain.repository

import id.ardion.quran.domain.model.LastRead
import id.ardion.quran.domain.model.MushafLastRead
import id.ardion.quran.domain.model.SurahLastRead
import kotlinx.coroutines.flow.StateFlow

interface LastReadRepository {
    val lastRead: StateFlow<LastRead>
    fun saveSurah(position: SurahLastRead)
    fun saveMushaf(position: MushafLastRead)
    fun clearSurah()
    fun clearMushaf()
}
