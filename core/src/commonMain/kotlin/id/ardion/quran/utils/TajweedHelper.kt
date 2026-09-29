package id.ardion.quran.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

private data class StyleRange(
    val start: Int,
    var end: Int,
    val color: Color,
    val tag: String = ""
)

data class TajweedInfo(
    val name: String,
    val description: String,
    val color: Color
)

fun getTajweedInfo(tag: String): TajweedInfo {
    return when (tag) {
        "q" -> TajweedInfo(
            name = "Qalqalah",
            description = "Memantulkan suara huruf Qalqalah (ق, ط, ب, ج, د) ketika berharakat sukun atau berada di akhir ayat (waqaf).",
            color = Color(0xFFDD0008)
        )
        "g", "n" -> TajweedInfo(
            name = "Ghunnah",
            description = "Mendengungkan suara pada huruf Nun atau Mim yang bertasydid selama 2 harakat.",
            color = Color(0xFFFF7E1E)
        )
        "i" -> TajweedInfo(
            name = "Ikhfa Haqiqi",
            description = "Menyamarkan suara Nun mati atau Tanwin saat bertemu 15 huruf Ikhfa dengan mendengungkan bacaan.",
            color = Color(0xFF26BFFD)
        )
        "p" -> TajweedInfo(
            name = "Iqlab",
            description = "Menukar suara Nun mati atau Tanwin menjadi bunyi Mim mati yang didengungkan saat bertemu huruf Ba (ب).",
            color = Color(0xFF4050FF)
        )
        "m" -> TajweedInfo(
            name = "Idgham Bighunnah",
            description = "Memasukkan suara Nun mati atau Tanwin ke dalam huruf berikutnya (ي, ن, م, و) disertai dengung.",
            color = Color(0xFF000EBC)
        )
        "o" -> TajweedInfo(
            name = "Idgham Bilaghunnah",
            description = "Memasukkan suara Nun mati atau Tanwin ke dalam huruf Lam (ل) atau Ra (ر) tanpa dengung.",
            color = Color(0xFF2144C1)
        )
        "c" -> TajweedInfo(
            name = "Mad Jaiz Munfashil",
            description = "Memanjangkan bacaan huruf mad yang bertemu hamzah di kata terpisah sepanjang 2, 4, atau 5 harakat.",
            color = Color(0xFFD500B7)
        )
        "f" -> TajweedInfo(
            name = "Mad Wajib Muttashil",
            description = "Memanjangkan bacaan huruf mad yang bertemu hamzah dalam satu kata sepanjang 4 atau 5 harakat.",
            color = Color(0xFF9400A8)
        )
        "w" -> TajweedInfo(
            name = "Idgham Shafawi",
            description = "Memasukkan bunyi Mim mati ke dalam huruf Mim berikutnya yang disertai dengung.",
            color = Color(0xFF58B800)
        )
        "a" -> TajweedInfo(
            name = "Ikhfa Shafawi",
            description = "Menyamarkan pengucapan Mim mati saat bertemu dengan huruf Ba (ب) disertai dengung.",
            color = Color(0xFF169777)
        )
        "u" -> TajweedInfo(
            name = "Izhar / Izhar Shafawi",
            description = "Membaca bunyi Nun mati, Tanwin, atau Mim mati secara jelas dan tegas tanpa dengung.",
            color = Color(0xFF169200)
        )
        "h", "s", "l" -> TajweedInfo(
            name = "Hamzatul Wasl / Silent",
            description = "Huruf Alif/Hamzah atau huruf mati yang dilewati dan tidak dibaca nyaring saat membaca secara sambung (wasal).",
            color = Color(0xFFAAAAAA)
        )
        "d", "b" -> TajweedInfo(
            name = "Tafkhim / Tarqiq",
            description = "Aturan pengucapan tebal atau tipis pada huruf Ra atau Lafdzul Jalalah (Allah).",
            color = Color(0xFFA1A1A1)
        )
        else -> TajweedInfo(
            name = "Hukum Tajwid",
            description = "Aturan tata cara membaca Al-Quran dengan tartil, fasih, dan benar.",
            color = Color(0xFF0A5C36)
        )
    }
}

fun parseTajweedText(rawText: String): AnnotatedString {
    val cleanTextBuilder = StringBuilder()
    val activeStyles = mutableListOf<StyleRange>()
    val completedStyles = mutableListOf<StyleRange>()

    var i = 0
    while (i < rawText.length) {
        var shouldSkip = false
        when {
            rawText[i] == '[' && i + 1 < rawText.length -> {
                val remaining = rawText.substring(i)
                val match = Regex("^\\[([a-z]):?\\d*\\[").find(remaining)
                if (match != null) {
                    val tag = match.groupValues[1]
                    activeStyles.add(
                        StyleRange(
                            cleanTextBuilder.length,
                            cleanTextBuilder.length,
                            getTajweedColor(tag),
                            tag
                        )
                    )
                    i += match.value.length
                    shouldSkip = true
                } else {
                    i++
                    shouldSkip = true
                }
            }
            rawText[i] == ']' -> {
                if (activeStyles.isNotEmpty()) {
                    val style = activeStyles.removeAt(activeStyles.size - 1)
                    style.end = cleanTextBuilder.length
                    if (style.end > style.start) completedStyles.add(style)
                }
                i++
                shouldSkip = true
            }
        }

        if (shouldSkip) continue

        val char = rawText[i]
        val code = char.code

        val processedChar = if (char == 'ٲ') '\u0670' else char

        val isInvalid = code < 32 || code == 0xFEFF || code == 0xFFFD ||
                (code in 0x200B..0x200F) || (code in 0x202A..0x202E) || (code in 0x2060..0x206F)

        if (!isInvalid) {
            if (code == 160 || code == 0x2007 || code == 0x202F) {
                cleanTextBuilder.append(' ')
            } else {
                cleanTextBuilder.append(processedChar)
            }
        }
        i++
    }

    for (style in activeStyles) {
        style.end = cleanTextBuilder.length
        if (style.end > style.start) completedStyles.add(style)
    }

    val cleanText = cleanTextBuilder.toString()

    return buildAnnotatedString {
        append(cleanText)

        for (style in completedStyles) {
            if (style.color != Color.Unspecified) {
                addStyle(SpanStyle(color = style.color), style.start, style.end)
            }
            if (style.tag.isNotEmpty()) {
                addStringAnnotation(
                    tag = "TAJWEED",
                    annotation = style.tag,
                    start = style.start,
                    end = style.end
                )
            }
        }
    }
}

private fun getTajweedColor(tag: String): Color {
    return when (tag) {
        "h", "s", "l" -> Color(0xFFAAAAAA)
        "n" -> Color(0xFF537FFF)
        "p" -> Color(0xFF4050FF)
        "m" -> Color(0xFF000EBC)
        "q" -> Color(0xFFDD0008)
        "o" -> Color(0xFF2144C1)
        "c" -> Color(0xFFD500B7)
        "f" -> Color(0xFF9400A8)
        "w" -> Color(0xFF58B800)
        "i" -> Color(0xFF26BFFD)
        "a" -> Color(0xFF169777)
        "u" -> Color(0xFF169200)
        "d", "b" -> Color(0xFFA1A1A1)
        "g" -> Color(0xFFFF7E1E)
        else -> Color.Unspecified
    }
}
