package id.ardion.quran.presentation.prayer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.presentation.components.ErrorScreen
import id.ardion.quran.presentation.components.SurahListShimmer
import id.ardion.quran.state.UiState
import id.ardion.quran.theme.IslamicBackground
import id.ardion.quran.theme.IslamicGold
import id.ardion.quran.theme.IslamicPrimary
import id.ardion.quran.theme.IslamicTextPrimary
import id.ardion.quran.theme.IslamicTextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import bacalah.app.shared.generated.resources.*
import org.jetbrains.compose.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
    viewModel: PrayerViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val phoneAzimuth by viewModel.compassSensor.azimuth.collectAsState()
    val sensorAccuracy by viewModel.compassSensor.accuracy.collectAsState()
    val isFlat by viewModel.compassSensor.isFlat.collectAsState()
    var permissionGranted by remember { mutableStateOf(false) }

    // Minta izin lokasi di screen ini, bukan di MainActivity
    LocationPermissionRequest(
        onPermissionGranted = {
            if (!permissionGranted) {
                permissionGranted = true
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
                is UiState.Idle, is UiState.Loading -> SurahListShimmer()

                is UiState.Error -> ErrorScreen(
                    message = current.message,
                    onRetry = { viewModel.loadPrayerTimes() }
                )

                is UiState.Success -> PrayerContent(
                    prayerTimes = current.data,
                    phoneAzimuth = phoneAzimuth,
                    sensorAccuracy = sensorAccuracy,
                    isFlat = isFlat
                )
            }
        }
    }
}

@Composable
fun PrayerContent(
    prayerTimes: PrayerTimes,
    phoneAzimuth: Float,
    sensorAccuracy: Int,
    isFlat: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = IslamicGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(Res.string.location_region),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Badge(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = prayerTimes.dateReadable,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = prayerTimes.locationName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(Res.string.coord_format, formatCoord(prayerTimes.latitude), formatCoord(prayerTimes.longitude)),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Swiveling Qibla Compass
                    QiblaCompassView(
                        qiblaBearing = prayerTimes.qiblaBearing,
                        phoneAzimuth = phoneAzimuth,
                        sensorAccuracy = sensorAccuracy,
                        isFlat = isFlat
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(Res.string.prayer_today_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IslamicTextPrimary
                )
            }
        }

        item { PrayerItemCard(stringResource(Res.string.prayer_fajr), prayerTimes.fajr, Icons.Default.WbTwilight) }
        item { PrayerItemCard(stringResource(Res.string.prayer_sunrise), prayerTimes.sunrise, Icons.Default.WbSunny) }
        item { PrayerItemCard(stringResource(Res.string.prayer_dhuhr), prayerTimes.dhuhr, Icons.Default.WbSunny) }
        item { PrayerItemCard(stringResource(Res.string.prayer_asr), prayerTimes.asr, Icons.Default.WbSunny) }
        item { PrayerItemCard(stringResource(Res.string.prayer_maghrib), prayerTimes.maghrib, Icons.Default.WbTwilight) }
        item { PrayerItemCard(stringResource(Res.string.prayer_isha), prayerTimes.isha, Icons.Default.WbTwilight) }
    }
}

@Composable
fun KaabaVectorIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = Color(0xFF1A1A1A),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )

        drawRect(
            color = IslamicGold,
            topLeft = Offset(0f, h * 0.22f),
            size = Size(w, h * 0.14f)
        )

        drawRoundRect(
            color = IslamicGold,
            topLeft = Offset(w * 0.58f, h * 0.48f),
            size = Size(w * 0.28f, h * 0.45f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

@Composable
fun QiblaCompassView(
    qiblaBearing: Double,
    phoneAzimuth: Float,
    sensorAccuracy: Int,
    isFlat: Boolean,
    modifier: Modifier = Modifier
) {
    // Otomatis muncul di awal saat masuk ke layar kompas
    var showCalibrationDialog by remember { mutableStateOf(true) }
    var showHowToUseDialog by remember { mutableStateOf(false) }

    val diffAngle = ((qiblaBearing - phoneAzimuth + 540) % 360) - 180
    val absDiff = kotlin.math.abs(diffAngle).toInt()
    val isFacingQibla = absDiff <= 5
    val turnRight = diffAngle > 0

    val targetRose = (-phoneAzimuth + 360f) % 360f
    var continuousRose by remember { mutableStateOf(targetRose) }

    LaunchedEffect(phoneAzimuth) {
        val currentMod = (continuousRose % 360f + 360f) % 360f
        var diff = targetRose - currentMod
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        continuousRose += diff
    }

    val animatedRose by animateFloatAsState(
        targetValue = continuousRose,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    val activeColor = if (isFacingQibla) Color(0xFF10B981) else IslamicGold

    if (showHowToUseDialog) {
        AlertDialog(
            onDismissRequest = { showHowToUseDialog = false },
            title = {
                Text(
                    text = stringResource(Res.string.how_to_use_title),
                    fontWeight = FontWeight.Bold,
                    color = IslamicTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(Res.string.how_to_use_heading),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IslamicTextPrimary
                    )
                    Text(
                        text = stringResource(Res.string.how_to_use_step1),
                        fontSize = 12.sp,
                        color = IslamicTextSecondary
                    )
                    Text(
                        text = stringResource(Res.string.how_to_use_step2),
                        fontSize = 12.sp,
                        color = IslamicTextSecondary
                    )
                    Text(
                        text = stringResource(Res.string.how_to_use_step3),
                        fontSize = 12.sp,
                        color = IslamicTextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHowToUseDialog = false }) {
                    Text(stringResource(Res.string.understand), color = IslamicPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = IslamicPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.calibration_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = IslamicTextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Visual Ilustrasi Angka 8
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(IslamicPrimary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📱 ⟳ ∞ ⟲",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(Res.string.calibration_instruction_head),
                                fontSize = 11.sp,
                                color = IslamicTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (sensorAccuracy >= 2) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (sensorAccuracy >= 2) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (sensorAccuracy >= 2) stringResource(Res.string.sensor_status_ready) else stringResource(Res.string.sensor_status_calibrating),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sensorAccuracy >= 2) Color(0xFF065F46) else Color(0xFF92400E)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(Res.string.how_to_use_step1),
                            fontSize = 12.sp,
                            color = IslamicTextPrimary
                        )
                        Text(
                            text = stringResource(Res.string.how_to_use_step2),
                            fontSize = 12.sp,
                            color = IslamicTextPrimary
                        )
                        Text(
                            text = stringResource(Res.string.how_to_use_step3),
                            fontSize = 12.sp,
                            color = IslamicTextPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCalibrationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.start_search_qibla),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (sensorAccuracy <= 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF3C7))
                        .clickable { showCalibrationDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        stringResource(Res.string.lbl_calibration_instruction),
                        color = Color(0xFF92400E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (!isFlat) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
//                        text = "Pegang HP Mendatar",
                        text = stringResource(Res.string.lbl_how_to_use_title),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
//                        text = "Sensor Siap & Akurat",
                        text = stringResource(Res.string.lbl_how_to_use_step),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showHowToUseDialog = true }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = stringResource(Res.string.instruction_cd),
                    tint = IslamicGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.how_to_use_btn),
                    color = IslamicGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
                .border(2.5.dp, activeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(rotationZ = animatedRose),
                contentAlignment = Alignment.Center
            ) {
                // Garis-garis derajat kompas
                Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    for (i in 0 until 360 step 15) {
                        val rad = (i - 90) * (PI / 180.0)
                        val isMajor = i % 90 == 0
                        val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                        val startX = center.x + (radius - tickLength) * cos(rad).toFloat()
                        val startY = center.y + (radius - tickLength) * sin(rad).toFloat()
                        val endX = center.x + radius * cos(rad).toFloat()
                        val endY = center.y + radius * sin(rad).toFloat()

                        drawLine(
                            color = if (isMajor) IslamicGold else Color.White.copy(alpha = 0.35f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight().padding(top = 14.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.cardinal_north_short),
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color(0xFFEF4444))
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = 90f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight().padding(top = 16.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.cardinal_east_short),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = 180f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight().padding(top = 16.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.cardinal_south_short),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = 270f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight().padding(top = 16.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.cardinal_west_short),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Jarum Kiblat (Ikon Ka'bah di derajat qiblaBearing)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(rotationZ = qiblaBearing.toFloat()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight().padding(top = 6.dp)
                    ) {
                        KaabaVectorIcon(modifier = Modifier.size(22.dp))
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(activeColor)
                        )
                    }
                }
            }

            // Indikator Titik Puncak Ponsel (Fixed Top Heading Notch)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 2.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 8.dp)
                        .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                        .background(activeColor)
                )
            }

            // Lingkaran Tengah (Informasi Derajat & Arah Hadap)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(IslamicPrimary)
                    .border(1.5.dp, activeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${phoneAzimuth.toInt()}°",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getHeadingDirection(phoneAzimuth),
                        color = activeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card Bimbingan Navigasi Dinamis (Informatif & Jelas)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFacingQibla) Color(0xFF065F46) else Color.White.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isFacingQibla) 3.dp else 0.dp)
        ) {
            if (isFacingQibla) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(Res.string.lbl_qibla_facing_correct),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = stringResource(Res.string.qibla_facing_desc, formatDegree(qiblaBearing)),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IslamicGold.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (turnRight) Icons.AutoMirrored.Filled.RotateRight else Icons.AutoMirrored.Filled.RotateLeft,
                            contentDescription = null,
                            tint = IslamicGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (turnRight) stringResource(Res.string.qibla_turn_right, absDiff) else stringResource(Res.string.qibla_turn_left, absDiff),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = stringResource(Res.string.qibla_align_instruction),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(Res.string.qibla_target_label, formatDegree(qiblaBearing)),
                color = IslamicGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = { showCalibrationDialog = true },
                modifier = Modifier.size(22.dp).padding(start = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(Res.string.calibration_guide_cd),
                    tint = IslamicGold.copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
fun PrayerItemCard(name: String, time: String, icon: ImageVector) {
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

private fun formatCoord(value: Double): String {
    val rounded = (value * 10000).toInt() / 10000.0
    return rounded.toString()
}

private fun formatDegree(value: Double): String {
    val rounded = (value * 10).toInt() / 10.0
    return rounded.toString()
}

@Composable
private fun getHeadingDirection(azimuth: Float): String {
    val a = (azimuth % 360f + 360f) % 360f
    return when {
        a >= 337.5 || a < 22.5 -> stringResource(Res.string.heading_north)
        a < 67.5 -> stringResource(Res.string.heading_northeast)
        a < 112.5 -> stringResource(Res.string.heading_east)
        a < 157.5 -> stringResource(Res.string.heading_southeast)
        a < 202.5 -> stringResource(Res.string.heading_south)
        a < 247.5 -> stringResource(Res.string.heading_southwest)
        a < 292.5 -> stringResource(Res.string.heading_west)
        else -> stringResource(Res.string.heading_northwest)
    }
}
