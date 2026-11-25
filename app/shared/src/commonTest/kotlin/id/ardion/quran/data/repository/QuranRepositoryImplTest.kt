package id.ardion.quran.data.repository

import id.ardion.quran.data.remote.QuranApiService
import id.ardion.quran.exception.NetworkException
import id.ardion.quran.fake.FakeResponseCache
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val SURAH_LIST_JSON =
    """{"code":200,"status":"OK","data":[{"number":1,"name":"الفاتحة","englishName":"Al-Faatiha","englishNameTranslation":"The Opening","numberOfAyahs":7,"revelationType":"Meccan"}]}"""
private const val EMPTY_LIST_JSON = """{"code":200,"status":"OK","data":[]}"""

class QuranRepositoryImplTest {
    private var requests = 0

    private fun repository(cache: FakeResponseCache, body: () -> String): QuranRepositoryImpl {
        val engine = MockEngine {
            requests++
            respond(body(), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { url("https://example.test/") }
        }
        return QuranRepositoryImpl(QuranApiService(client), cache)
    }

    @Test
    fun cacheMissFetchesAndStoresResponse() = runTest {
        val cache = FakeResponseCache()
        val result = repository(cache) { SURAH_LIST_JSON }.getAllSurahs()

        assertEquals("Al-Faatiha", result.getOrThrow().single().englishName)
        assertEquals(1, requests)
        assertTrue(cache.entries.keys.any { it.endsWith("surah_list") })
    }

    @Test
    fun cacheHitSkipsNetwork() = runTest {
        val cache = FakeResponseCache()
        repository(cache) { SURAH_LIST_JSON }.getAllSurahs()
        val offline = repository(cache) { error("network must not be used") }

        assertEquals(1, offline.getAllSurahs().getOrThrow().size)
        assertEquals(1, requests)
    }

    @Test
    fun emptyDataIsReportedAsEmptyResponse() = runTest {
        val result = repository(FakeResponseCache()) { EMPTY_LIST_JSON }.getAllSurahs()
        assertEquals(NetworkException.EmptyResponse, result.exceptionOrNull())
    }

    @Test
    fun networkFailureWithoutCacheFails() = runTest {
        val result = repository(FakeResponseCache()) { error("offline") }.getAllSurahs()
        assertTrue(result.isFailure)
    }

    @Test
    fun corruptCacheEntryIsReplacedFromNetwork() = runTest {
        val cache = FakeResponseCache(mapOf("v1_surah_list" to "{broken"))
        val result = repository(cache) { SURAH_LIST_JSON }.getAllSurahs()

        assertTrue(result.isSuccess)
        assertEquals(1, requests)
    }
}
