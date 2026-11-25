package id.ardion.quran.presentation.mushaf

import id.ardion.quran.audio.AudioQueueController
import id.ardion.quran.domain.model.MushafLastRead
import id.ardion.quran.domain.usecase.ClearMushafLastReadUseCase
import id.ardion.quran.domain.usecase.GetLastReadUseCase
import id.ardion.quran.domain.usecase.GetPageDetailUseCase
import id.ardion.quran.domain.usecase.SaveMushafLastReadUseCase
import id.ardion.quran.fake.FakeAudioPlayback
import id.ardion.quran.fake.FakeLastReadRepository
import id.ardion.quran.fake.FakeQuranRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MushafPageViewModelTest {
    private val quran = FakeQuranRepository()
    private val lastRead = FakeLastReadRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = MushafPageViewModel(
        GetPageDetailUseCase(quran),
        GetLastReadUseCase(lastRead),
        SaveMushafLastReadUseCase(lastRead),
        ClearMushafLastReadUseCase(lastRead),
        AudioQueueController(FakeAudioPlayback())
    )

    @Test
    fun revisitedPageIsServedFromMemory() {
        val viewModel = viewModel()
        viewModel.loadPage(1)
        viewModel.loadPage(2)
        viewModel.loadPage(1)
        assertEquals(listOf(1, 2), quran.pageRequests)
    }

    @Test
    fun sameAyahNumberInAnotherSurahIsADifferentBookmark() {
        val viewModel = viewModel()
        viewModel.toggleLastRead(604, 1, "Al-Ikhlaas")
        viewModel.toggleLastRead(604, 1, "An-Naas")
        assertEquals("An-Naas", viewModel.lastRead.value?.surahName)

        viewModel.toggleLastRead(604, 1, "An-Naas")
        assertNull(viewModel.lastRead.value)
    }

    @Test
    fun initialPageComesFromLastRead() {
        lastRead.saveMushaf(MushafLastRead(pageNumber = 50))
        assertEquals(50, viewModel().initialPageNumber)
    }
}
