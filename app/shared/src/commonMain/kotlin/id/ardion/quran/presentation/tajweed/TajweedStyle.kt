package id.ardion.quran.presentation.tajweed

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import id.ardion.quran.tajweed.TajweedText

const val TAJWEED_ANNOTATION_TAG = "TAJWEED"

data class TajweedInfo(
    val name: String,
    val arabicName: String,
    val description: String,
    val color: Color
)

private val hamzatulWasl = TajweedInfo(
    name = "Hamzat ul Wasl / Silent / Laam Shamsiyyah",
    arabicName = "همزة الوصل / لام شمسية",
    description = "Huruf tidak dibaca nyaring atau dilewati saat membaca sambung (sukun, hamzatul wasl, atau lam shamsiyyah).",
    color = Color(0xFFAAAAAA)
)

private val idghamMutajanisayn = TajweedInfo(
    name = "Idgham Mutajanisayn / Mutaqaribayn",
    arabicName = "إدغام متجانسين",
    description = "Peleburan dua huruf yang berdekatan atau sama makhraj pengucapannya.",
    color = Color(0xFFA1A1A1)
)

private val tajweedRules: Map<String, TajweedInfo> = mapOf(
    "h" to hamzatulWasl,
    "s" to hamzatulWasl,
    "l" to hamzatulWasl,
    "n" to TajweedInfo(
        name = "Madda Normal (Mad 'Adi)",
        arabicName = "مد عادي",
        description = "Memanjangkan bacaan mad biasa sepanjang 2 harakat.",
        color = Color(0xFF537FFF)
    ),
    "p" to TajweedInfo(
        name = "Madda Permissible (Mad Jaiz)",
        arabicName = "مد جائز",
        description = "Memanjangkan bacaan mad jaiz sepanjang 2, 4, atau 6 harakat.",
        color = Color(0xFF4050FF)
    ),
    "m" to TajweedInfo(
        name = "Madda Necessary (Mad Wajib)",
        arabicName = "مد واجب",
        description = "Memanjangkan bacaan mad wajib sepanjang 6 harakat.",
        color = Color(0xFF000EBC)
    ),
    "q" to TajweedInfo(
        name = "Qalaqah",
        arabicName = "قلقلة",
        description = "Memantulkan suara huruf Qalqalah (ق, ط, ب, ج, د) ketika berharakat sukun atau waqaf.",
        color = Color(0xFFDD0008)
    ),
    "o" to TajweedInfo(
        name = "Madda Obligatory (Mad Lazim)",
        arabicName = "مد لازم",
        description = "Memanjangkan bacaan mad lazim sepanjang 4 sampai 5 harakat.",
        color = Color(0xFF2144C1)
    ),
    "c" to TajweedInfo(
        name = "Ikhafa Shafawi (Ikhfa Shafawi)",
        arabicName = "إخفاء شفوي",
        description = "Menyamarkan pengucapan Mim mati saat bertemu dengan huruf Ba (ب) disertai dengung.",
        color = Color(0xFFD500B7)
    ),
    "f" to TajweedInfo(
        name = "Ikhafa (Ikhfa Haqiqi)",
        arabicName = "إخفاء",
        description = "Menyamarkan suara Nun mati atau Tanwin saat bertemu huruf ikhfa dengan dengung.",
        color = Color(0xFF9400A8)
    ),
    "w" to TajweedInfo(
        name = "Idgham Shafawi",
        arabicName = "إدغام شفوي",
        description = "Memasukkan bunyi Mim mati ke dalam huruf Mim berikutnya yang disertai dengung.",
        color = Color(0xFF58B800)
    ),
    "i" to TajweedInfo(
        name = "Iqlab",
        arabicName = "إقلاب",
        description = "Menukar suara Nun mati atau Tanwin menjadi bunyi Mim mati yang didengungkan saat bertemu huruf Ba (ب).",
        color = Color(0xFF26BFFD)
    ),
    "a" to TajweedInfo(
        name = "Idgham With Ghunnah (Idgham Bighunnah)",
        arabicName = "إدغام بغنة",
        description = "Memasukkan suara Nun mati atau Tanwin ke dalam huruf berikutnya (ي, ن, م, و) disertai dengung.",
        color = Color(0xFF169777)
    ),
    "u" to TajweedInfo(
        name = "Idgham Without Ghunnah (Idgham Bilaghunnah)",
        arabicName = "إدغام بلا غنة",
        description = "Memasukkan suara Nun mati atau Tanwin ke dalam huruf Lam (ل) atau Ra (ر) tanpa dengung.",
        color = Color(0xFF169200)
    ),
    "d" to idghamMutajanisayn,
    "b" to idghamMutajanisayn,
    "g" to TajweedInfo(
        name = "Ghunnah",
        arabicName = "غنة",
        description = "Mendengungkan suara pada huruf Nun atau Mim yang bertasydid selama 2 harakat.",
        color = Color(0xFFFF7E1E)
    )
)

private val genericTajweedInfo = TajweedInfo(
    name = "Hukum Tajwid",
    arabicName = "تجويد",
    description = "Aturan tata cara membaca Al-Quran dengan tartil, fasih, dan benar.",
    color = Color(0xFF0A5C36)
)

fun getTajweedInfo(tag: String): TajweedInfo = tajweedRules[tag] ?: genericTajweedInfo

fun TajweedText.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    append(text)
    for (span in spans) {
        tajweedRules[span.rule]?.let { addStyle(SpanStyle(color = it.color), span.start, span.end) }
        addStringAnnotation(TAJWEED_ANNOTATION_TAG, span.rule, span.start, span.end)
    }
}
