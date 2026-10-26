package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserSettings(
    @DocumentId val id: String = "current",
    val userId: String = "",
    val useAutoLocation: Boolean = true,
    val cityName: String = "",
    val countryName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezoneHours: Double = 0.0,
    val methodId: String = "umm_al_qura",
    val madhhabId: String = "shafi",
    val voiceId: String = "makkah",
    val fajrVoiceId: String = "fajr_makkah",
    val volume: Double = 0.8,
    @get:PropertyName("isDarkTheme") @set:PropertyName("isDarkTheme") var isDarkTheme: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestorePrayerLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateString: String = "",
    val prayerType: String = "",
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted") var isCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
