package id.ardion.quran.data.repository

import id.ardion.quran.data.local.ResponseCache
import id.ardion.quran.data.mapper.mapToPageDetail
import id.ardion.quran.data.mapper.mapToSurahDetail
import id.ardion.quran.data.mapper.toDomain
import id.ardion.quran.data.remote.QuranApiService
import id.ardion.quran.data.response.MultiEditionSurahDto
import id.ardion.quran.data.response.PageDataDto
import id.ardion.quran.data.response.SurahListItemDto
import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.model.SurahItem
import id.ardion.quran.domain.repository.QuranRepository
import id.ardion.quran.exception.NetworkException
import id.ardion.quran.logging.logWarning
import id.ardion.quran.result.runCatchingCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private const val TAG = "QuranRepository"
private const val CACHE_VERSION = "v1"

class QuranRepositoryImpl(
    private val apiService: QuranApiService,
    private val cache: ResponseCache
) : QuranRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getAllSurahs(): Result<List<SurahItem>> = cacheFirst(
        key = "surah_list",
        serializer = ListSerializer(SurahListItemDto.serializer()),
        fetch = { apiService.getAllSurahs() },
        map = { dtos -> dtos.toDomain().takeIf { it.isNotEmpty() } }
    )

    override suspend fun getSurahDetail(surahNumber: Int): Result<SurahDetail> = cacheFirst(
        key = "surah_$surahNumber",
        serializer = ListSerializer(MultiEditionSurahDto.serializer()),
        fetch = { apiService.getSurahDetailMultiEdition(surahNumber) },
        map = ::mapToSurahDetail
    )

    override suspend fun getPageDetail(pageNumber: Int): Result<PageDetail> = cacheFirst(
        key = "page_$pageNumber",
        serializer = PageDataDto.serializer(),
        fetch = { apiService.getPageTajweed(pageNumber) },
        map = { mapToPageDetail(it, pageNumber) }
    )

    private suspend fun <D : Any, T : Any> cacheFirst(
        key: String,
        serializer: KSerializer<D>,
        fetch: suspend () -> D?,
        map: (D) -> T?
    ): Result<T> = withContext(Dispatchers.Default) {
        val cacheKey = "${CACHE_VERSION}_$key"
        readCache(cacheKey, serializer)?.let(map)?.let { return@withContext Result.success(it) }

        runCatchingCancellable { fetch() }.fold(
            onSuccess = { dto ->
                val mapped = dto?.let(map)
                if (dto != null && mapped != null) {
                    cache.write(cacheKey, json.encodeToString(serializer, dto))
                    Result.success(mapped)
                } else {
                    Result.failure(NetworkException.EmptyResponse)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    private fun <D> readCache(key: String, serializer: KSerializer<D>): D? {
        val raw = cache.read(key) ?: return null
        return try {
            json.decodeFromString(serializer, raw)
        } catch (e: IllegalArgumentException) {
            logWarning(TAG, "Dropping unreadable cache entry $key", e)
            cache.remove(key)
            null
        }
    }
}
