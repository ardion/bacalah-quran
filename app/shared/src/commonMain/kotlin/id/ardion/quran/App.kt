package id.ardion.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import id.ardion.quran.di.appModule
import id.ardion.quran.domain.model.ReadMode
import id.ardion.quran.navigation.NavRoute
import id.ardion.quran.presentation.home.DashboardContent
import id.ardion.quran.presentation.home.HomeViewModel
import id.ardion.quran.presentation.mushaf.MushafPageScreen
import id.ardion.quran.presentation.mushaf.MushafPageViewModel
import id.ardion.quran.presentation.prayer.PrayerScreen
import id.ardion.quran.presentation.prayer.PrayerViewModel
import id.ardion.quran.presentation.splash.SplashScreen
import id.ardion.quran.presentation.surah.SurahDetailScreen
import id.ardion.quran.presentation.surah.SurahDetailViewModel
import id.ardion.quran.presentation.surahlist.SurahListScreen
import id.ardion.quran.presentation.surahlist.SurahListViewModel
import id.ardion.quran.theme.IslamicBackground
import id.ardion.quran.theme.IslamicPrimary
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        MaterialTheme {
            var isSplashVisible by remember { mutableStateOf(true) }
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                delay(1800)
                isSplashVisible = false
            }

            Box(modifier = Modifier.fillMaxSize().background(IslamicBackground)) {
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
                    NavHost(
                        navController = navController,
                        startDestination = NavRoute.Dashboard
                    ) {
                        composable<NavRoute.Dashboard> {
                            val homeViewModel: HomeViewModel = koinInject()
                            val multiLastRead by homeViewModel.multiLastRead.collectAsState()

                            DashboardContent(
                                multiLastRead = multiLastRead,
                                onOpenSurahList = { navController.navigate(NavRoute.SurahList) },
                                onOpenMushaf = { navController.navigate(NavRoute.MushafPage) },
                                onOpenPrayerTimes = { navController.navigate(NavRoute.PrayerTimes) },
                                onContinueReading = { lastReadInfo ->
                                    if (lastReadInfo.readMode == ReadMode.MUSHAF) {
                                        navController.navigate(NavRoute.MushafPage)
                                    } else {
                                        navController.navigate(NavRoute.SurahDetail(lastReadInfo.surahNumber))
                                    }
                                }
                            )
                        }

                        composable<NavRoute.SurahList> {
                            val listViewModel: SurahListViewModel = koinInject()
                            SurahListScreen(
                                viewModel = listViewModel,
                                onSurahClick = { surahNum ->
                                    navController.navigate(NavRoute.SurahDetail(surahNum))
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable<NavRoute.MushafPage> {
                            val mushafViewModel: MushafPageViewModel = koinInject()
                            val homeViewModel: HomeViewModel = koinInject()
                            val multiLastRead by homeViewModel.multiLastRead.collectAsState()
                            val mushafPage = multiLastRead.mushafLastRead?.pageNumber ?: 1

                            MushafPageScreen(
                                viewModel = mushafViewModel,
                                initialPageNumber = mushafPage,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable<NavRoute.PrayerTimes> {
                            val prayerViewModel: PrayerViewModel = koinInject()
                            PrayerScreen(
                                viewModel = prayerViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable<NavRoute.SurahDetail> { backStackEntry ->
                            val route = backStackEntry.toRoute<NavRoute.SurahDetail>()
                            val detailViewModel: SurahDetailViewModel = koinInject()

                            LaunchedEffect(route.surahNumber) {
                                detailViewModel.loadSurah(route.surahNumber)
                            }

                            SurahDetailScreen(
                                viewModel = detailViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
