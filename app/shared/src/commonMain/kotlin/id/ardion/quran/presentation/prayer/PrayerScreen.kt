package id.ardion.quran.presentation.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ardion.quran.domain.model.PrayerTimeSource
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.presentation.components.ErrorScreen
import id.ardion.quran.presentation.components.SurahListShimmer
import id.ardion.quran.presentation.reminder.ReminderSettingsCard
import id.ardion.quran.presentation.reminder.ReminderSettingsViewModel
import id.ardion.quran.state.UiState
import id.ardion.quran.presentation.theme.IslamicBackground
import id.ardion.quran.presentation.theme.IslamicPrimary
import id.ardion.quran.presentation.theme.IslamicTextPrimary
import id.ardion.quran.presentation.theme.IslamicTextSecondary
import bacalah.app.shared.generated.resources.*
import org.jetbrains.compose.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
    viewModel: PrayerViewModel,
    reminderViewModel: ReminderSettingsViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val compass by viewModel.compass.collectAsState()
    var hasRequestedLocation by remember { mutableStateOf(false) }

    LocationPermissionRequest(
        onResult = {
            if (!hasRequestedLocation) {
                hasRequestedLocation = true
                viewModel.loadPrayerTimes()
            }
        }
    )

    DisposableEffect(Unit) {
        viewModel.startCompass()
        onDispose {
            viewModel.stopCompass()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.prayer_screen_title),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPrayerTimes() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(Res.string.refresh),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IslamicPrimary)
            )
        },
        containerColor = IslamicBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val current = state) {
                UiState.Loading -> SurahListShimmer()

                is UiState.Error -> ErrorScreen(
                    error = current.cause,
                    onRetry = { viewModel.loadPrayerTimes() }
                )

                is UiState.Success -> PrayerContent(
                    prayerTimes = current.data,
                    compass = compass,
                    reminderViewModel = reminderViewModel
                )
            }
        }
    }
}

@Composable
fun PrayerContent(
    prayerTimes: PrayerTimes,
    compass: CompassReading,
    reminderViewModel: ReminderSettingsViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LocationAndQiblaCard(
                prayerTimes = prayerTimes,
                compass = compass
            )
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(Res.string.prayer_today_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IslamicTextPrimary
                )
                Text(
                    text = stringResource(
                        when (prayerTimes.source) {
                            PrayerTimeSource.KEMENAG -> Res.string.prayer_source_kemenag
                            PrayerTimeSource.CALCULATED -> Res.string.prayer_source_calculated
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicTextSecondary
                )
            }
        }

        item { PrayerItemCard(stringResource(Res.string.prayer_fajr), prayerTimes.fajr, Icons.Default.WbTwilight) }
        item { PrayerItemCard(stringResource(Res.string.prayer_sunrise), prayerTimes.sunrise, Icons.Default.WbSunny) }
        item { PrayerItemCard(stringResource(Res.string.prayer_dhuhr), prayerTimes.dhuhr, Icons.Default.WbSunny) }
        item { PrayerItemCard(stringResource(Res.string.prayer_asr), prayerTimes.asr, Icons.Default.WbSunny) }
        item { PrayerItemCard(stringResource(Res.string.prayer_maghrib), prayerTimes.maghrib, Icons.Default.WbTwilight) }
        item { PrayerItemCard(stringResource(Res.string.prayer_isha), prayerTimes.isha, Icons.Default.WbTwilight) }

        item { ReminderSettingsCard(reminderViewModel) }
    }
}

@Composable
private fun PrayerItemCard(name: String, time: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(IslamicPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = IslamicPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IslamicTextPrimary
                )
            }

            Text(
                text = time,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicPrimary
            )
        }
    }
}
