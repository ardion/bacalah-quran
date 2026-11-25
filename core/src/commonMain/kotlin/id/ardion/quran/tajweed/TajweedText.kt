package id.ardion.quran.tajweed

data class TajweedText(
    val text: String,
    val spans: List<TajweedSpan> = emptyList()
)

data class TajweedSpan(
    val start: Int,
    val end: Int,
    val rule: String
)

private val OPEN_TAG = Regex("""\[([a-z]):?\d*\[""")

private const val SUPERSCRIPT_ALEF_LOOKALIKE = '\u0672'
private const val SUPERSCRIPT_ALEF = '\u0670'

fun parseTajweedText(rawText: String): TajweedText {
    val out = StringBuilder(rawText.length)
    val open = ArrayDeque<Pair<Int, String>>()
    val spans = mutableListOf<TajweedSpan>()

    fun close(start: Int, rule: String) {
        if (out.length > start) spans += TajweedSpan(start, out.length, rule)
    }

    var i = 0
    while (i < rawText.length) {
        val char = rawText[i]
        when {
            char == '[' -> {
                val match = OPEN_TAG.matchAt(rawText, i)
                if (match != null) {
                    open.addLast(out.length to match.groupValues[1])
                    i += match.value.length
                } else {
                    i++
                }
            }
            char == ']' -> {
                open.removeLastOrNull()?.let { (start, rule) -> close(start, rule) }
                i++
            }
            else -> {
                appendCleanChar(out, char)
                i++
            }
        }
    }

    open.forEach { (start, rule) -> close(start, rule) }

    return TajweedText(out.toString(), spans)
}

private fun appendCleanChar(out: StringBuilder, char: Char) {
    val code = char.code
    val isInvisible = code < 32 || code == 0xFEFF || code == 0xFFFD ||
        code in 0x200B..0x200F || code in 0x202A..0x202E || code in 0x2060..0x206F
    when {
        isInvisible -> Unit
        code == 0x00A0 || code == 0x2007 || code == 0x202F -> out.append(' ')
        char == SUPERSCRIPT_ALEF_LOOKALIKE -> out.append(SUPERSCRIPT_ALEF)
        else -> out.append(char)
    }
}
