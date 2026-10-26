package com.example.calculation

import com.example.model.CalculationMethod
import com.example.model.DailyPrayerTimes
import com.example.model.Madhhab
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object PrayerTimesCalculator {

    private const val KAABA_LAT = 21.422487
    private const val KAABA_LON = 39.826206
    private const val EARTH_RADIUS_KM = 6371.0

    fun calculateDailyPrayerTimes(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timezoneHours: Double,
        cityName: String,
        method: CalculationMethod,
        madhhab: Madhhab,
        offsets: Map<PrayerType, Int> = emptyMap(),
        adhanToggles: Map<PrayerType, Boolean> = emptyMap()
    ): DailyPrayerTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Day
        val jd = julianDate(year, month, day)
        val d = jd - 2451545.0

        // Sun's coordinates
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d

        val ra = fixAngle(
            Math.toDegrees(
                atan2(
                    cos(Math.toRadians(e)) * sin(Math.toRadians(l)),
                    cos(Math.toRadians(l))
                )
            )
        ) / 15.0

        val declination = Math.toDegrees(
            asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l)))
        )

        val eqTime = (q / 15.0) - ra

        // Midday (Dhuhr)
        val noon = 12.0 + timezoneHours - (longitude / 15.0) - eqTime

        // Sunrise & Sunset angle (accounting for atmospheric refraction)
        val sunriseAngle = -0.833
        val sunriseHourAngle = hourAngle(latitude, declination, sunriseAngle)
        val sunrise = noon - (sunriseHourAngle / 15.0)
        val sunset = noon + (sunriseHourAngle / 15.0)

        // Fajr
        val fajrHourAngle = hourAngle(latitude, declination, -method.fajrAngle)
        val fajr = noon - (fajrHourAngle / 15.0)

        // Asr (Shadow ratio: 1 for Shafi, 2 for Hanafi)
        val asrAltitude = Math.toDegrees(
            atan(1.0 / (madhhab.shadowRatio + kotlin.math.tan(Math.toRadians(abs(latitude - declination)))))
        )
        val asrHourAngle = hourAngle(latitude, declination, asrAltitude)
        val asr = noon + (asrHourAngle / 15.0)

        // Maghrib
        val maghrib = sunset

        // Isha
        val isha = if (method.ishaFixedMinutes > 0) {
            maghrib + (method.ishaFixedMinutes / 60.0)
        } else {
            val ishaHourAngle = hourAngle(latitude, declination, -method.ishaAngle)
            noon + (ishaHourAngle / 15.0)
        }

        val rawTimes = mapOf(
            PrayerType.FAJR to fajr,
            PrayerType.SUNRISE to sunrise,
            PrayerType.DHUHR to noon,
            PrayerType.ASR to asr,
            PrayerType.MAGHRIB to maghrib,
            PrayerType.ISHA to isha
        )

        val baseCal = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nowMillis = System.currentTimeMillis()

        val prayerItems = PrayerType.values().map { type ->
            val hours = rawTimes[type] ?: 12.0
            val offset = offsets[type] ?: 0
            val prayerCal = (baseCal.clone() as Calendar).apply {
                val totalMinutes = (hours * 60).toInt() + offset
                set(Calendar.HOUR_OF_DAY, (totalMinutes / 60) % 24)
                set(Calendar.MINUTE, totalMinutes % 60)
            }

            val millis = prayerCal.timeInMillis
            PrayerTimeItem(
                type = type,
                timeMillis = millis,
                formattedTime = formatTimeArabic(prayerCal),
                offsetMinutes = offset,
                adhanEnabled = adhanToggles[type] ?: (type != PrayerType.SUNRISE)
            )
        }

        // Determine current and next prayer
        var currentPrayer: PrayerTimeItem? = null
        var nextPrayer: PrayerTimeItem? = null

        for (i in prayerItems.indices) {
            val item = prayerItems[i]
            if (nowMillis >= item.timeMillis) {
                currentPrayer = item
            } else if (nextPrayer == null) {
                nextPrayer = item
            }
        }

        // If after Isha, next prayer is tomorrow's Fajr
        val remainingMillis = if (nextPrayer != null) {
            nextPrayer.timeMillis - nowMillis
        } else {
            // Tomorrow's Fajr
            val tomorrowFajrCal = (baseCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
                val totalMinutes = ((rawTimes[PrayerType.FAJR] ?: 5.0) * 60).toInt() + (offsets[PrayerType.FAJR] ?: 0)
                set(Calendar.HOUR_OF_DAY, (totalMinutes / 60) % 24)
                set(Calendar.MINUTE, totalMinutes % 60)
            }
            nextPrayer = prayerItems.first().copy(timeMillis = tomorrowFajrCal.timeInMillis)
            tomorrowFajrCal.timeInMillis - nowMillis
        }

        val updatedItems = prayerItems.map { item ->
            item.copy(
                isNext = (item.type == nextPrayer?.type),
                isCurrent = (item.type == currentPrayer?.type)
            )
        }

        val qibla = calculateQibla(latitude, longitude)
        val distance = calculateDistanceToMakkah(latitude, longitude)

        return DailyPrayerTimes(
            prayers = updatedItems,
            currentPrayer = currentPrayer,
            nextPrayer = nextPrayer,
            remainingMillisToNext = kotlin.math.max(0L, remainingMillis),
            gregorianDateText = formatGregorianDateArabic(calendar.time),
            hijriDateText = getHijriDateArabic(calendar),
            cityName = cityName,
            qiblaAngle = qibla,
            distanceToMakkahKm = distance
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + (a / 4)
        return (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + b - 1524.5
    }

    private fun hourAngle(lat: Double, decl: Double, alt: Double): Double {
        val latRad = Math.toRadians(lat)
        val declRad = Math.toRadians(decl)
        val altRad = Math.toRadians(alt)

        val cosH = (sin(altRad) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosH))
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    fun calculateQibla(lat: Double, lon: Double): Double {
        val latRad = Math.toRadians(lat)
        val lonRad = Math.toRadians(lon)
        val kaabaLatRad = Math.toRadians(KAABA_LAT)
        val kaabaLonRad = Math.toRadians(KAABA_LON)

        val dLon = kaabaLonRad - lonRad
        val y = sin(dLon)
        val x = cos(latRad) * kotlin.math.tan(kaabaLatRad) - sin(latRad) * cos(dLon)
        var qibla = Math.toDegrees(atan2(y, x))
        qibla = (qibla + 360.0) % 360.0
        return qibla
    }

    fun calculateDistanceToMakkah(lat: Double, lon: Double): Double {
        val lat1 = Math.toRadians(lat)
        val lon1 = Math.toRadians(lon)
        val lat2 = Math.toRadians(KAABA_LAT)
        val lon2 = Math.toRadians(KAABA_LON)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    private fun formatTimeArabic(calendar: Calendar): String {
        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val isAm = hour24 < 12
        val hour12 = when (val h = hour24 % 12) {
            0 -> 12
            else -> h
        }
        val period = if (isAm) "ص" else "م"
        return String.format(Locale.US, "%02d:%02d %s", hour12, minute, period)
    }

    private fun formatGregorianDateArabic(date: Date): String {
        val dayNames = arrayOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
        val monthNames = arrayOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )
        val cal = Calendar.getInstance().apply { time = date }
        val dayOfWeek = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = monthNames[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)
        return "$dayOfWeek، $day $month $year م"
    }

    fun getHijriDateArabic(calendar: Calendar): String {
        // High-precision algorithmic Hijri conversion
        val jd = julianDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        val l = (jd - 1948440 + 10632).toInt()
        val n = ((l - 1) / 10631).toInt()
        val l1 = l - 10631 * n + 354
        val j = (((10985 - l1) / 5316).toInt()) * (((50 * l1) / 17719).toInt()) +
                ((l1 / 5670).toInt()) * (((43 * l1) / 15238).toInt())
        val l2 = l1 - (((30 - j) / 15).toInt()) * (((17719 * j) / 50).toInt()) -
                ((j / 16).toInt()) * (((15238 * j) / 43).toInt()) + 29
        val m = ((24 * l2) / 709).toInt()
        val d = l2 - ((709 * m) / 24).toInt()
        val y = 30 * n + j - 30

        val hijriMonths = arrayOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        val monthIndex = (m - 1).coerceIn(0, 11)
        return "$d ${hijriMonths[monthIndex]} $y هـ"
    }
}
