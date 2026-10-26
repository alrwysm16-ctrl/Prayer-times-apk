package com.example.model

enum class PrayerType(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val isObligatory: Boolean = true
) {
    FAJR("fajr", "الفجر", "Fajr"),
    SUNRISE("sunrise", "الشروق", "Sunrise", false),
    DHUHR("dhuhr", "الظهر", "Dhuhr"),
    ASR("asr", "العصر", "Asr"),
    MAGHRIB("maghrib", "المغرب", "Maghrib"),
    ISHA("isha", "العشاء", "Isha")
}

enum class CalculationMethod(
    val id: String,
    val arabicName: String,
    val description: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaFixedMinutes: Int = 0 // Umm Al-Qura uses 90 min fixed
) {
    UMM_AL_QURA(
        id = "umm_al_qura",
        arabicName = "أم القرى (مكة المكرمة)",
        description = "فجر: 18.5°، عشاء: بعد المغرب بـ 90 دقيقة",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaFixedMinutes = 90
    ),
    MUSLIM_WORLD_LEAGUE(
        id = "mwl",
        arabicName = "رابطة العالم الإسلامي",
        description = "فجر: 18°، عشاء: 17°",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    EGYPTIAN(
        id = "egyptian",
        arabicName = "الهيئة المصرية العامة للمساحة",
        description = "فجر: 19.5°، عشاء: 17.5°",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    KARACHI(
        id = "karachi",
        arabicName = "جامعة العلوم الإسلامية بكراتشي",
        description = "فجر: 18°، عشاء: 18°",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    NORTH_AMERICA(
        id = "isna",
        arabicName = "أمريكا الشمالية (ISNA)",
        description = "فجر: 15°، عشاء: 15°",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    )
}

enum class Madhhab(
    val id: String,
    val arabicName: String,
    val shadowRatio: Double
) {
    SHAFI("shafi", "الجمهور (الشافعي، الحنبلي، المالكي)", 1.0),
    HANAFI("hanafi", "الحنفي (مثلي ظل الشيء)", 2.0)
}

data class CityLocation(
    val nameArabic: String,
    val nameEnglish: String,
    val countryArabic: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneHours: Double
)

data class PrayerTimeItem(
    val type: PrayerType,
    val timeMillis: Long,
    val formattedTime: String,
    val isNext: Boolean = false,
    val isCurrent: Boolean = false,
    val offsetMinutes: Int = 0,
    val adhanEnabled: Boolean = true
)

data class DailyPrayerTimes(
    val prayers: List<PrayerTimeItem>,
    val currentPrayer: PrayerTimeItem?,
    val nextPrayer: PrayerTimeItem?,
    val remainingMillisToNext: Long,
    val gregorianDateText: String,
    val hijriDateText: String,
    val cityName: String,
    val qiblaAngle: Double,
    val distanceToMakkahKm: Double
)

data class AppSettings(
    val useAutoLocation: Boolean = true,
    val selectedCity: CityLocation,
    val method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
    val madhhab: Madhhab = Madhhab.SHAFI,
    val offsets: Map<PrayerType, Int> = emptyMap(),
    val adhanToggles: Map<PrayerType, Boolean> = PrayerType.values().associateWith { it != PrayerType.SUNRISE },
    val selectedVoiceId: String = "makkah",
    val fajrVoiceId: String = "fajr_makkah",
    val volume: Float = 0.8f,
    val isDarkTheme: Boolean = false
)
