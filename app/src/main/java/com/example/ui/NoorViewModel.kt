package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AdhanVoice
import com.example.audio.SoundManager
import com.example.calculation.PrayerTimesCalculator
import com.example.data.PreferencesManager
import com.example.model.AppSettings
import com.example.model.CalculationMethod
import com.example.model.CityLocation
import com.example.model.DailyPrayerTimes
import com.example.model.Madhhab
import com.example.model.PrayerType
import com.example.sensor.CompassSensorManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class NoorScreen(val titleArabic: String) {
    HOME("الرئيسية"),
    QIBLA("القبلة"),
    AZKAR("الأذكار والتسبيح"),
    SETTINGS("الإعدادات")
}

data class TasbihState(
    val currentPhrase: String = "سبحان الله",
    val count: Int = 0,
    val target: Int = 33,
    val totalCycles: Int = 0
)

class NoorViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    val soundManager = SoundManager(application)
    val compassSensorManager = CompassSensorManager(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _currentScreen = MutableStateFlow(NoorScreen.HOME)
    val currentScreen: StateFlow<NoorScreen> = _currentScreen.asStateFlow()

    private val _settings = MutableStateFlow(preferencesManager.loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _prayerTimes = MutableStateFlow<DailyPrayerTimes?>(null)
    val prayerTimes: StateFlow<DailyPrayerTimes?> = _prayerTimes.asStateFlow()

    private val _isGpsLoading = MutableStateFlow(false)
    val isGpsLoading: StateFlow<Boolean> = _isGpsLoading.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    // Daily Prayer Tracker state (100% offline & local)
    private val todayString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private val _todayPrayerLogs = MutableStateFlow<Map<String, Boolean>>(preferencesManager.loadPrayerLogs(todayString))
    val todayPrayerLogs: StateFlow<Map<String, Boolean>> = _todayPrayerLogs.asStateFlow()

    // Compass State
    val compassAzimuth = compassSensorManager.azimuth
    val compassAccuracy = compassSensorManager.accuracy
    val isSensorAvailable = compassSensorManager.isSensorAvailable

    private val _manualCompassHeading = MutableStateFlow(0f)
    val manualCompassHeading: StateFlow<Float> = _manualCompassHeading.asStateFlow()

    // Tasbih State
    private val _tasbihState = MutableStateFlow(TasbihState())
    val tasbihState: StateFlow<TasbihState> = _tasbihState.asStateFlow()

    init {
        recalculatePrayers()
        startPeriodicTimer()
    }

    fun togglePrayerTrack(prayerType: PrayerType) {
        val currentCompleted = _todayPrayerLogs.value[prayerType.id] ?: false
        val newStatus = !currentCompleted
        val updatedMap = _todayPrayerLogs.value.toMutableMap()
        updatedMap[prayerType.id] = newStatus
        _todayPrayerLogs.value = updatedMap

        preferencesManager.savePrayerLog(todayString, prayerType.id, newStatus)
    }

    fun setScreen(screen: NoorScreen) {
        _currentScreen.value = screen
        if (screen == NoorScreen.QIBLA) {
            compassSensorManager.startListening()
        } else {
            compassSensorManager.stopListening()
        }
    }

    override fun onCleared() {
        super.onCleared()
        compassSensorManager.stopListening()
        soundManager.stopAdhan()
    }

    private fun startPeriodicTimer() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                recalculatePrayers()
            }
        }
    }

    fun recalculatePrayers() {
        val currentSettings = _settings.value
        val city = currentSettings.selectedCity
        val calendar = Calendar.getInstance()

        val daily = PrayerTimesCalculator.calculateDailyPrayerTimes(
            calendar = calendar,
            latitude = city.latitude,
            longitude = city.longitude,
            timezoneHours = city.timezoneHours,
            cityName = city.nameArabic,
            method = currentSettings.method,
            madhhab = currentSettings.madhhab,
            offsets = currentSettings.offsets,
            adhanToggles = currentSettings.adhanToggles
        )
        _prayerTimes.value = daily
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        preferencesManager.updateSettings(newSettings)
        recalculatePrayers()
    }

    fun setCity(city: CityLocation) {
        val updated = _settings.value.copy(
            selectedCity = city,
            useAutoLocation = false
        )
        updateSettings(updated)
    }

    fun setMethod(method: CalculationMethod) {
        val updated = _settings.value.copy(method = method)
        updateSettings(updated)
    }

    fun setMadhhab(madhhab: Madhhab) {
        val updated = _settings.value.copy(madhhab = madhhab)
        updateSettings(updated)
    }

    fun setOffset(type: PrayerType, offset: Int) {
        val currentOffsets = _settings.value.offsets.toMutableMap()
        currentOffsets[type] = offset.coerceIn(-60, 60)
        val updated = _settings.value.copy(offsets = currentOffsets)
        updateSettings(updated)
    }

    fun toggleAdhan(type: PrayerType, enabled: Boolean) {
        val currentToggles = _settings.value.adhanToggles.toMutableMap()
        currentToggles[type] = enabled
        val updated = _settings.value.copy(adhanToggles = currentToggles)
        updateSettings(updated)
    }

    fun setSelectedVoice(voiceId: String) {
        val updated = _settings.value.copy(selectedVoiceId = voiceId)
        updateSettings(updated)
    }

    fun setFajrVoice(voiceId: String) {
        val updated = _settings.value.copy(fajrVoiceId = voiceId)
        updateSettings(updated)
    }

    fun setVolume(volume: Float) {
        val updated = _settings.value.copy(volume = volume.coerceIn(0f, 1f))
        updateSettings(updated)
    }

    fun toggleTheme(isDark: Boolean) {
        val updated = _settings.value.copy(isDarkTheme = isDark)
        updateSettings(updated)
    }

    fun testAudioVoice(voice: AdhanVoice) {
        soundManager.playAdhan(voice, _settings.value.volume)
    }

    fun stopAudio() {
        soundManager.stopAdhan()
    }

    fun setManualHeading(heading: Float) {
        _manualCompassHeading.value = (heading + 360f) % 360f
    }

    // Tasbih actions
    fun incrementTasbih() {
        val current = _tasbihState.value
        val newCount = current.count + 1
        if (newCount >= current.target) {
            val phrases = listOf("سبحان الله", "الحمد لله", "الله أكبر", "لا إله إلا الله وحده لا شريك له")
            val currentIndex = phrases.indexOf(current.currentPhrase)
            val nextIndex = (currentIndex + 1) % phrases.size
            val nextTarget = if (nextIndex == 3) 1 else 33
            _tasbihState.value = current.copy(
                currentPhrase = phrases[nextIndex],
                count = 0,
                target = nextTarget,
                totalCycles = current.totalCycles + 1
            )
        } else {
            _tasbihState.value = current.copy(count = newCount)
        }
        compassSensorManager.checkAndTriggerHaptic(true)
    }

    fun resetTasbih() {
        _tasbihState.value = TasbihState()
    }

    fun selectTasbihPhrase(phrase: String) {
        _tasbihState.value = _tasbihState.value.copy(
            currentPhrase = phrase,
            count = 0,
            target = 33
        )
    }

    @SuppressLint("MissingPermission")
    fun fetchGpsLocation() {
        _isGpsLoading.value = true
        _locationError.value = null

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                _isGpsLoading.value = false
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val tz = TimeZone.getDefault().rawOffset / 3600000.0

                    var cityName = "موقعي الحالي"
                    try {
                        val geocoder = Geocoder(getApplication(), Locale("ar"))
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "موقعي الحالي"
                        }
                    } catch (_: Exception) {
                    }

                    val detectedCity = CityLocation(
                        nameArabic = cityName,
                        nameEnglish = "Current Location",
                        countryArabic = "",
                        latitude = lat,
                        longitude = lon,
                        timezoneHours = tz
                    )

                    val updated = _settings.value.copy(
                        useAutoLocation = true,
                        selectedCity = detectedCity
                    )
                    updateSettings(updated)
                } else {
                    _locationError.value = "تعذر تحديد الموقع بدقة، تأكد من تشغيل GPS"
                }
            }
            .addOnFailureListener { e ->
                _isGpsLoading.value = false
                _locationError.value = "حدث خطأ أثناء جلب الموقع: ${e.localizedMessage}"
            }
    }
}
