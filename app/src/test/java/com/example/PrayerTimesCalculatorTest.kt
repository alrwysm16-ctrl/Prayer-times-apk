package com.example

import com.example.calculation.PrayerTimesCalculator
import com.example.model.CalculationMethod
import com.example.model.Madhhab
import com.example.model.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class PrayerTimesCalculatorTest {

    @Test
    fun testPrayerTimesCalculation() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 25, 12, 0, 0)
        }

        // Makkah coordinates
        val lat = 21.3891
        val lon = 39.8579
        val tz = 3.0

        val result = PrayerTimesCalculator.calculateDailyPrayerTimes(
            calendar = calendar,
            latitude = lat,
            longitude = lon,
            timezoneHours = tz,
            cityName = "مكة المكرمة",
            method = CalculationMethod.UMM_AL_QURA,
            madhhab = Madhhab.SHAFI
        )

        assertNotNull(result)
        assertEquals(6, result.prayers.size)
        assertTrue(result.hijriDateText.contains("هـ"))
        assertTrue(result.distanceToMakkahKm < 50.0) // Within Makkah region
    }

    @Test
    fun testQiblaAngle() {
        // Riyadh to Makkah: ~245 degrees
        val riyadhLat = 24.7136
        val riyadhLon = 46.6753
        val qibla = PrayerTimesCalculator.calculateQibla(riyadhLat, riyadhLon)

        assertTrue("Qibla angle from Riyadh should be around 240-250 degrees", qibla in 235.0..255.0)
    }
}
