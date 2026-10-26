package com.example.data

import com.example.model.CityLocation

object CitiesData {
    val defaultCity = CityLocation("الرياض", "Riyadh", "المملكة العربية السعودية", 24.7136, 46.6753, 3.0)

    val cities = listOf(
        CityLocation("مكة المكرمة", "Makkah", "المملكة العربية السعودية", 21.3891, 39.8579, 3.0),
        CityLocation("المدينة المنورة", "Madinah", "المملكة العربية السعودية", 24.5247, 39.5692, 3.0),
        defaultCity,
        CityLocation("جدة", "Jeddah", "المملكة العربية السعودية", 21.5433, 39.1728, 3.0),
        CityLocation("الدمام", "Dammam", "المملكة العربية السعودية", 26.4207, 50.0888, 3.0),
        CityLocation("أبها", "Abha", "المملكة العربية السعودية", 18.2164, 42.5053, 3.0),
        CityLocation("القاهرة", "Cairo", "مصر", 30.0444, 31.2357, 2.0),
        CityLocation("الإسكندرية", "Alexandria", "مصر", 31.2001, 29.9187, 2.0),
        CityLocation("دبي", "Dubai", "الإمارات العربية المتحدة", 25.2048, 55.2708, 4.0),
        CityLocation("أبوظبي", "Abu Dhabi", "الإمارات العربية المتحدة", 24.4539, 54.3773, 4.0),
        CityLocation("الدوحة", "Doha", "قطر", 25.2854, 51.5310, 3.0),
        CityLocation("الكويت", "Kuwait City", "الكويت", 29.3759, 47.9774, 3.0),
        CityLocation("المنامة", "Manama", "البحرين", 26.2285, 50.5860, 3.0),
        CityLocation("مسقط", "Muscat", "عُمان", 23.5880, 58.3829, 4.0),
        CityLocation("عمان", "Amman", "الأردن", 31.9454, 35.9284, 3.0),
        CityLocation("القدس الشريف", "Jerusalem", "فلسطين", 31.7683, 35.2137, 2.0),
        CityLocation("غزة", "Gaza", "فلسطين", 31.5017, 34.4668, 2.0),
        CityLocation("بيروت", "Beirut", "لبنان", 33.8938, 35.5018, 2.0),
        CityLocation("دمشق", "Damascus", "سوريا", 33.5138, 36.2765, 3.0),
        CityLocation("بغداد", "Baghdad", "العراق", 33.3152, 44.3661, 3.0),
        CityLocation("البصرة", "Basra", "العراق", 30.5085, 47.7804, 3.0),
        CityLocation("صنعاء", "Sanaa", "اليمن", 15.3694, 44.1910, 3.0),
        CityLocation("الخرطوم", "Khartoum", "السودان", 15.5007, 32.5599, 2.0),
        CityLocation("طرابلس", "Tripoli", "ليبيا", 32.8872, 13.1913, 2.0),
        CityLocation("تونس", "Tunis", "تونس", 36.8065, 10.1815, 1.0),
        CityLocation("الجزائر", "Algiers", "الجزائر", 36.7538, 3.0588, 1.0),
        CityLocation("الرباط", "Rabat", "المغرب", 34.0209, -6.8416, 1.0),
        CityLocation("الدار البيضاء", "Casablanca", "المغرب", 33.5731, -7.5898, 1.0),
        CityLocation("نواكشوط", "Nouakchott", "موريتانيا", 18.0735, -15.9582, 0.0),
        CityLocation("إسطنبول", "Istanbul", "تركيا", 41.0082, 28.9784, 3.0),
        CityLocation("أنقرة", "Ankara", "تركيا", 39.9334, 32.8597, 3.0),
        CityLocation("لندن", "London", "المملكة المتحدة", 51.5074, -0.1278, 0.0),
        CityLocation("باريس", "Paris", "فرنسا", 48.8566, 2.3522, 1.0),
        CityLocation("برلين", "Berlin", "ألمانيا", 52.5200, 13.4050, 1.0),
        CityLocation("نيويورك", "New York", "الولايات المتحدة", 40.7128, -74.0060, -5.0),
        CityLocation("تورونتو", "Toronto", "كندا", 43.6532, -79.3832, -5.0),
        CityLocation("كوالالمبور", "Kuala Lumpur", "ماليزيا", 3.1390, 101.6869, 8.0),
        CityLocation("جاكرتا", "Jakarta", "إندونيسيا", -6.2088, 106.8456, 7.0)
    )
}
