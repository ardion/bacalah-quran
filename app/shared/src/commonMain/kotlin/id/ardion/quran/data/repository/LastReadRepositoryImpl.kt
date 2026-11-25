package id.ardion.quran.data.repository

import id.ardion.quran.data.local.KeyValueStore
import id.ardion.quran.data.local.LastReadEntity
import id.ardion.quran.data.local.toEntity
import id.ardion.quran.data.local.toMushafLastRead
import id.ardion.quran.data.local.toSurahLastRead
import id.ardion.quran.domain.model.LastRead
import id.ardion.quran.domain.model.MushafLastRead
import id.ardion.quran.domain.model.SurahLastRead
import id.ardion.quran.domain.repository.LastReadRepository
import id.ardion.quran.logging.logWarning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

private const val TAG = "LastReadRepository"
internal const val KEY_SURAH_LAST_READ = "key_surah_last_read"
internal const val KEY_MUSHAF_LAST_READ = "key_mushaf_last_read"

class LastReadRepositoryImpl(private val store: KeyValueStore) : LastReadRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val _lastRead = MutableStateFlow(
        LastRead(
            surah = load(KEY_SURAH_LAST_READ)?.toSurahLastRead(),
            mushaf = load(KEY_MUSHAF_LAST_READ)?.toMushafLastRead()
        )
    )
    override val lastRead: StateFlow<LastRead> = _lastRead.asStateFlow()

    override fun saveSurah(position: SurahLastRead) {
        save(KEY_SURAH_LAST_READ, position.toEntity())
        _lastRead.update { it.copy(surah = position) }
    }

    override fun saveMushaf(position: MushafLastRead) {
        save(KEY_MUSHAF_LAST_READ, position.toEntity())
        _lastRead.update { it.copy(mushaf = position) }
    }

    override fun clearSurah() {
        store.remove(KEY_SURAH_LAST_READ)
        _lastRead.update { it.copy(surah = null) }
    }

    override fun clearMushaf() {
        store.remove(KEY_MUSHAF_LAST_READ)
        _lastRead.update { it.copy(mushaf = null) }
    }

    private fun load(key: String): LastReadEntity? {
        val raw = store.getString(key) ?: return null
        return try {
            json.decodeFromString<LastReadEntity>(raw)
        } catch (e: IllegalArgumentException) {
            logWarning(TAG, "Discarding unreadable value for $key", e)
            store.remove(key)
            null
        }
    }

    private fun save(key: String, entity: LastReadEntity) {
        store.putString(key, json.encodeToString(entity))
    }
}
