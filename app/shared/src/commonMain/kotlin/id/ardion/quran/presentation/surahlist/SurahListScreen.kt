package id.ardion.quran.presentation.surahlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.Res
import bacalah.app.shared.generated.resources.amiri
import id.ardion.quran.domain.model.SurahItem
import id.ardion.quran.state.UiState
import org.jetbrains.compose.resources.Font

val IslamicPrimary = Color(0xFF0A5C36)
val IslamicGold = Color(0xFFC5A059)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahListScreen(
    viewModel: SurahListViewModel,
    onSurahClick: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Al-Quran Al-Kareem",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IslamicPrimary
                )
            )
        },
        containerColor = Color(0xFFF4F6F4)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val current = state) {
                is UiState.Idle, is UiState.Loading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = IslamicPrimary
                )

                is UiState.Error -> Text(
                    current.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )

                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(current.data) { surah ->
                            SurahListItem(surah = surah, onClick = { onSurahClick(surah.number) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahListItem(surah: SurahItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(IslamicPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${surah.number}",
                    color = IslamicPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.englishName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = surah.translation,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        containerColor = IslamicGold.copy(alpha = 0.15f),
                        contentColor = Color(0xFF8A6D2B)
                    ) {
                        Text(
                            text = surah.revelationType,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${surah.numberOfAyahs} Ayat",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = surah.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicPrimary,
                fontFamily = FontFamily(
                    Font(Res.font.amiri)
                )
            )
        }
    }
}
