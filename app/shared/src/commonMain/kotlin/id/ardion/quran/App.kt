package id.ardion.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import id.ardion.quran.navigation.NavRoute
import id.ardion.quran.platform.hasNativeSplashScreen
import id.ardion.quran.presentation.home.DashboardContent
import id.ardion.quran.presentation.home.HomeViewModel
import id.ardion.quran.presentation.mushaf.MushafPageScreen
import id.ardion.quran.presentation.prayer.PrayerScreen
import id.ardion.quran.presentation.splash.SplashScreen
import id.ardion.quran.presentation.surah.SurahDetailScreen
import id.ardion.quran.presentation.surahlist.SurahListScreen
import id.ardion.quran.presentation.theme.BacalahTheme
import id.ardion.quran.presentation.theme.IslamicBackground
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

private const val SPLASH_DURATION_MS = 700L

@Composable
fun App() {
    BacalahTheme {
        var isSplashVisible by remember { mutableStateOf(!hasNativeSplashScreen) }
        LaunchedEffect(Unit) {
            if (isSplashVisible) {
                delay(SPLASH_DURATION_MS)
                isSplashVisible = false
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(IslamicBackground)) {
            AnimatedVisibility(visible = isSplashVisible, enter = fadeIn(), exit = fadeOut()) {
                SplashScreen()
            }
            AnimatedVisibility(visible = !isSplashVisible, enter = fadeIn(), exit = fadeOut()) {
                AppNavHost(rememberNavController())
            }
        }
    }
}

@Composable
private fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavRoute.Dashboard) {
        composable<NavRoute.Dashboard> {
            val homeViewModel: HomeViewModel = koinViewModel()
            val lastRead by homeViewModel.lastRead.collectAsState()
            DashboardContent(
                lastRead = lastRead,
                onOpenSurahList = { navController.navigate(NavRoute.SurahList) },
                onOpenMushaf = { navController.navigate(NavRoute.MushafPage) },
                onOpenPrayerTimes = { navController.navigate(NavRoute.PrayerTimes) },
                onContinueSurah = { surahNumber -> navController.navigate(NavRoute.SurahDetail(surahNumber)) }
            )
        }

        composable<NavRoute.SurahList> {
            SurahListScreen(
                viewModel = koinViewModel(),
                onSurahClick = { surahNumber -> navController.navigate(NavRoute.SurahDetail(surahNumber)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<NavRoute.MushafPage> {
            MushafPageScreen(
                viewModel = koinViewModel(),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<NavRoute.PrayerTimes> {
            PrayerScreen(
                viewModel = koinViewModel(),
                reminderViewModel = koinViewModel(),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<NavRoute.SurahDetail> { backStackEntry ->
            SurahDetailScreen(
                surahNumber = backStackEntry.toRoute<NavRoute.SurahDetail>().surahNumber,
                viewModel = koinViewModel(),
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
