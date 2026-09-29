package id.ardion.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import id.ardion.quran.di.appModule
import id.ardion.quran.presentation.splash.SplashScreen
import id.ardion.quran.presentation.surah.SurahDetailScreen
import id.ardion.quran.presentation.surah.SurahDetailViewModel
import id.ardion.quran.presentation.surahlist.SurahListScreen
import id.ardion.quran.presentation.surahlist.SurahListViewModel
import kotlinx.coroutines.delay
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        MaterialTheme {
            var isSplashVisible by remember { mutableStateOf(true) }
            var selectedSurahNumber by remember { mutableStateOf<Int?>(null) }

            LaunchedEffect(Unit) {
                delay(1800)
                isSplashVisible = false
            }

            AnimatedVisibility(
                visible = isSplashVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SplashScreen()
            }

            AnimatedVisibility(
                visible = !isSplashVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (selectedSurahNumber == null) {
                    val listViewModel: SurahListViewModel = koinInject()
                    SurahListScreen(
                        viewModel = listViewModel,
                        onSurahClick = { surahNum -> selectedSurahNumber = surahNum }
                    )
                } else {
                    val detailViewModel: SurahDetailViewModel = koinInject()
                    LaunchedEffect(selectedSurahNumber) {
                        selectedSurahNumber?.let { detailViewModel.loadSurah(it) }
                    }
                    SurahDetailScreen(
                        viewModel = detailViewModel,
                        onBackClick = { selectedSurahNumber = null }
                    )
                }
            }
        }
    }
}
