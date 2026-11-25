package id.ardion.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bacalah.app.shared.generated.resources.Res
import bacalah.app.shared.generated.resources.close
import id.ardion.quran.presentation.theme.IslamicPrimary
import id.ardion.quran.presentation.theme.IslamicTextPrimary
import id.ardion.quran.presentation.tajweed.TAJWEED_ANNOTATION_TAG
import id.ardion.quran.presentation.tajweed.TajweedInfo
import org.jetbrains.compose.resources.stringResource

@Composable
fun TajweedInfoDialog(info: TajweedInfo, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorDot(info.color)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = info.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = { Text(text = info.description, fontSize = 15.sp, lineHeight = 22.sp) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.close), color = IslamicPrimary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier.size(14.dp).clip(CircleShape).background(color)
    )
}

@Composable
fun TappableText(
    text: AnnotatedString,
    style: TextStyle,
    onTapOffset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = text,
        style = style,
        onTextLayout = { layout = it },
        modifier = modifier.pointerInput(text) {
            detectTapGestures { position ->
                layout?.let { onTapOffset(it.getOffsetForPosition(position)) }
            }
        }
    )
}

fun AnnotatedString.tajweedRuleAt(offset: Int): String? {
    fun at(start: Int, end: Int) =
        getStringAnnotations(TAJWEED_ANNOTATION_TAG, start, end).firstOrNull()?.item
    return at(offset, offset)
        ?: at((offset - 1).coerceAtLeast(0), (offset + 1).coerceAtMost(length))
}
