package id.ardion.quran.presentation.mushaf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.presentation.theme.IslamicBackground
import id.ardion.quran.presentation.theme.IslamicPrimary
import id.ardion.quran.presentation.theme.IslamicTextPrimary
import id.ardion.quran.presentation.theme.IslamicTextSecondary
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private enum class JumpTarget(val tabLabel: StringResource, val inputLabel: StringResource, val max: Int) {
    PAGE(Res.string.mushaf_tab_page, Res.string.input_page_label, MUSHAF_PAGE_COUNT),
    SURAH(Res.string.mushaf_tab_surah, Res.string.input_surah_label, SURAH_COUNT),
    JUZ(Res.string.mushaf_tab_juz, Res.string.input_juz_label, JUZ_COUNT);

    fun toPage(number: Int): Int = when (this) {
        PAGE -> number.coerceIn(1, MUSHAF_PAGE_COUNT)
        SURAH -> getSurahStartPage(number)
        JUZ -> getJuzStartPage(number)
    }
}

@Composable
fun MushafJumpDialog(
    currentPageNumber: Int,
    onJumpToPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var target by remember { mutableStateOf(JumpTarget.PAGE) }
    var input by remember { mutableStateOf("$currentPageNumber") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.mushaf_jump_title),
                fontWeight = FontWeight.Bold,
                color = IslamicTextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                JumpTargetTabs(
                    selected = target,
                    onSelect = { tab ->
                        target = tab
                        input = if (tab == JumpTarget.PAGE) "$currentPageNumber" else "1"
                    }
                )

                Text(
                    text = stringResource(target.inputLabel),
                    fontSize = 13.sp,
                    color = IslamicTextSecondary
                )

                OutlinedTextField(
                    value = input,
                    onValueChange = { newInput ->
                        val isNumeric = newInput.all { it.isDigit() }
                        if (isNumeric && (newInput.toIntOrNull() ?: 0) <= target.max) input = newInput
                    },
                    placeholder = { Text(stringResource(Res.string.input_example_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicPrimary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { input.toIntOrNull()?.let { onJumpToPage(target.toPage(it)) } },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(Res.string.mushaf_open_nav), fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.mushaf_cancel), color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JumpTargetTabs(selected: JumpTarget, onSelect: (JumpTarget) -> Unit) {
    PrimaryTabRow(
        selectedTabIndex = selected.ordinal,
        containerColor = IslamicBackground,
        contentColor = IslamicPrimary
    ) {
        JumpTarget.entries.forEach { tab ->
            Tab(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                text = { Text(stringResource(tab.tabLabel), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }
    }
}
