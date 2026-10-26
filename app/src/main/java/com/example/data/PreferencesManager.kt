package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import com.example.model.CalculationMethod
import com.example.model.CityLocation
import com.example.model.Madhhab
import com.example.model.PrayerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("noor_app_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun loadSettings(): AppSettings {
        val useAutoLocation = prefs.getBoolean(KEY_AUTO_LOC, true)
        val cityName = prefs.getString(KEY_CITY_NAME, CitiesData.defaultCity.nameArabic) ?: CitiesData.defaultCity.nameArabic
        val cityEng = prefs.getString(KEY_CITY_ENG, CitiesData.defaultCity.nameEnglish) ?: CitiesData.defaultCity.nameEnglish
        val cityCountry = prefs.getString(KEY_CITY_COUNTRY, CitiesData.defaultCity.countryArabic) ?: CitiesData.defaultCity.countryArabic
        val cityLat = prefs.getFloat(KEY_CITY_LAT, CitiesData.defaultCity.latitude.toFloat()).toDouble()
        val cityLon = prefs.getFloat(KEY_CITY_LON, CitiesData.defaultCity.longitude.toFloat()).toDouble()
        val cityTz = prefs.getFloat(KEY_CITY_TZ, CitiesData.defaultCity.timezoneHours.toFloat()).toDouble()

        val city = CityLocation(cityName, cityEng, cityCountry, cityLat, cityLon, cityTz)

        val methodId = prefs.getString(KEY_METHOD, CalculationMethod.UMM_AL_QURA.id)
        val method = CalculationMethod.values().find { it.id == methodId } ?: CalculationMethod.UMM_AL_QURA

        val madhhabId = prefs.getString(KEY_MADHHAB, Madhhab.SHAFI.id)
        val madhhab = Madhhab.values().find { it.id == madhhabId } ?: Madhhab.SHAFI

        val offsets = PrayerType.values().associateWith { type ->
            prefs.getInt("offset_${type.id}", 0)
        }

        val adhanToggles = PrayerType.values().associateWith { type ->
            prefs.getBoolean("adhan_${type.id}", type != PrayerType.SUNRISE)
        }

        val selectedVoice = prefs.getString(KEY_VOICE, "makkah") ?: "makkah"
        val fajrVoice = prefs.getString(KEY_FAJR_VOICE, "fajr_makkah") ?: "fajr_makkah"
        val volume = prefs.getFloat(KEY_VOLUME, 0.8f)
        val isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, false)

        return AppSettings(
            useAutoLocation = useAutoLocation,
            selectedCity = city,
            method = method,
            madhhab = madhhab,
            offsets = offsets,
            adhanToggles = adhanToggles,
            selectedVoiceId = selectedVoice,
            fajrVoiceId = fajrVoice,
            volume = volume,
            isDarkTheme = isDarkTheme
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putBoolean(KEY_AUTO_LOC, newSettings.useAutoLocation)
            putString(KEY_CITY_NAME, newSettings.selectedCity.nameArabic)
            putString(KEY_CITY_ENG, newSettings.selectedCity.nameEnglish)
            putString(KEY_CITY_COUNTRY, newSettings.selectedCity.countryArabic)
            putFloat(KEY_CITY_LAT, newSettings.selectedCity.latitude.toFloat())
            putFloat(KEY_CITY_LON, newSettings.selectedCity.longitude.toFloat())
            putFloat(KEY_CITY_TZ, newSettings.selectedCity.timezoneHours.toFloat())

            putString(KEY_METHOD, newSettings.method.id)
            putString(KEY_MADHHAB, newSettings.madhhab.id)

            newSettings.offsets.forEach { (type, offset) ->
                putInt("offset_${type.id}", offset)
            }

            newSettings.adhanToggles.forEach { (type, enabled) ->
                putBoolean("adhan_${type.id}", enabled)
            }

            putString(KEY_VOICE, newSettings.selectedVoiceId)
            putString(KEY_FAJR_VOICE, newSettings.fajrVoiceId)
            putFloat(KEY_VOLUME, newSettings.volume)
            putBoolean(KEY_DARK_THEME, newSettings.isDarkTheme)
            apply()
        }
        _settings.value = newSettings
    }

    fun loadPrayerLogs(dateString: String): Map<String, Boolean> {
        return PrayerType.values().associate { type ->
            type.id to prefs.getBoolean("prayer_log_${dateString}_${type.id}", false)
        }
    }

    fun savePrayerLog(dateString: String, prayerType: String, completed: Boolean) {
        prefs.edit().putBoolean("prayer_log_${dateString}_$prayerType", completed).apply()
    }

    companion object {
        private const val KEY_AUTO_LOC = "key_auto_location"
        private const val KEY_CITY_NAME = "key_city_name"
        private const val KEY_CITY_ENG = "key_city_eng"
        private const val KEY_CITY_COUNTRY = "key_city_country"
        private const val KEY_CITY_LAT = "key_city_lat"
        private const val KEY_CITY_LON = "key_city_lon"
        private const val KEY_CITY_TZ = "key_city_tz"
        private const val KEY_METHOD = "key_method"
        private const val KEY_MADHHAB = "key_madhhab"
        private const val KEY_VOICE = "key_voice"
        private const val KEY_FAJR_VOICE = "key_fajr_voice"
        private const val KEY_VOLUME = "key_volume"
        private const val KEY_DARK_THEME = "key_dark_theme"
    }
}
