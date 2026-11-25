package id.ardion.quran.cache

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LruCacheTest {

    @Test
    fun evictsLeastRecentlyUsed() {
        val cache = LruCache<Int, String>(maxSize = 2)
        cache[1] = "a"
        cache[2] = "b"
        cache[1]
        cache[3] = "c"
        assertNull(cache[2])
        assertEquals("a", cache[1])
        assertEquals("c", cache[3])
        assertEquals(2, cache.size)
    }

    @Test
    fun missingKeyReturnsNull() = assertNull(LruCache<Int, String>(1)[42])
}
