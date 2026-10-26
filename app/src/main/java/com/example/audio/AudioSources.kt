package com.example.audio

/**
 * مصادر ملفات الأذان والتنبيهات
 * يمكنك استبدال الروابط هنا بروابط أو مسارات ملفاتك الصوتية لاحقاً بكل سهولة
 */
data class AdhanVoice(
    val id: String,
    val titleArabic: String,
    val muadhinArabic: String,
    val url: String,
    val isFajrOnly: Boolean = false,
    val durationSeconds: Int = 180
)

object AudioSources {
    // قائمة المؤذنين والأصوات المتاحة للتطبيق
    val voices: List<AdhanVoice> = listOf(
        AdhanVoice(
            id = "makkah",
            titleArabic = "أذان الحرم المكي الشريف",
            muadhinArabic = "الشيخ علي ملا",
            url = "https://media.sd.ma/assabile/adhan_3748238/5ecdc37ef694.mp3",
            isFajrOnly = false
        ),
        AdhanVoice(
            id = "fajr_makkah",
            titleArabic = "أذان فجر الحرم المكي (الصلاة خير من النوم)",
            muadhinArabic = "الشيخ علي ملا",
            url = "https://media.sd.ma/assabile/adhan_3748238/5ecdc37ef694.mp3",
            isFajrOnly = true
        ),
        AdhanVoice(
            id = "madinah",
            titleArabic = "أذان المسجد النبوي الشريف",
            muadhinArabic = "الشيخ عصام بخاري",
            url = "https://media.sd.ma/assabile/adhan_3748238/08798e404b90.mp3",
            isFajrOnly = false
        ),
        AdhanVoice(
            id = "fajr_madinah",
            titleArabic = "أذان فجر المسجد النبوي الشريف",
            muadhinArabic = "الشيخ عصام بخاري",
            url = "https://media.sd.ma/assabile/adhan_3748238/08798e404b90.mp3",
            isFajrOnly = true
        ),
        AdhanVoice(
            id = "alaqsa",
            titleArabic = "أذان المسجد الأقصى المبارك",
            muadhinArabic = "القدس الشريف",
            url = "https://media.sd.ma/assabile/adhan_3748238/a3fa41f17fe6.mp3",
            isFajrOnly = false
        ),
        AdhanVoice(
            id = "abdulbasit",
            titleArabic = "أذان الشيخ عبد الباسط عبد الصمد",
            muadhinArabic = "جمهورية مصر العربية",
            url = "https://media.sd.ma/assabile/adhan_3748238/5ecdc37ef694.mp3",
            isFajrOnly = false
        ),
        AdhanVoice(
            id = "mishary",
            titleArabic = "أذان الشيخ مشاري العفاسي",
            muadhinArabic = "دولة الكويت",
            url = "https://media.sd.ma/assabile/adhan_3748238/08798e404b90.mp3",
            isFajrOnly = false
        ),
        AdhanVoice(
            id = "takbeerat",
            titleArabic = "تكبيرات هادئة (تنبيه قصير)",
            muadhinArabic = "تكبيرات الصلاة",
            url = "builtin://synthetic_takbeerat",
            isFajrOnly = false
        ),
        AdhanVoice(
            id = "chime",
            titleArabic = "رنة تنبيه ناعمة (Chime)",
            muadhinArabic = "تنبيه إسلامي هادئ",
            url = "builtin://chime",
            isFajrOnly = false
        )
    )

    fun getVoiceById(id: String): AdhanVoice {
        return voices.find { it.id == id } ?: voices.first()
    }
}
