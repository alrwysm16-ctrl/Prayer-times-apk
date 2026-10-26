package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.FirestorePrayerLog
import com.example.data.model.FirestoreUserSettings
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NoorFirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            FirebaseApp.getInstance(),
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserSettings(): Flow<FirestoreUserSettings?> = flow {
        val uid = requireUserId()
        val path = "users/$uid/settings/current"
        emitAll(
            db.collection("users").document(uid).collection("settings").document("current")
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(FirestoreUserSettings::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun saveUserSettings(settings: FirestoreUserSettings) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("settings").document("current")
        try {
            val existing = docRef.get().await()
            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "useAutoLocation" to settings.useAutoLocation,
                "cityName" to settings.cityName,
                "countryName" to settings.countryName,
                "latitude" to settings.latitude,
                "longitude" to settings.longitude,
                "timezoneHours" to settings.timezoneHours,
                "methodId" to settings.methodId,
                "madhhabId" to settings.madhhabId,
                "voiceId" to settings.voiceId,
                "fajrVoiceId" to settings.fajrVoiceId,
                "volume" to settings.volume,
                "isDarkTheme" to settings.isDarkTheme,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (!existing.exists() || existing.get("createdAt") == null) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }

            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    fun observePrayerLogs(dateString: String): Flow<List<FirestorePrayerLog>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/prayer_logs"
        emitAll(
            db.collection("users").document(uid).collection("prayer_logs")
                .whereEqualTo("dateString", dateString)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FirestorePrayerLog::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun togglePrayerLog(dateString: String, prayerType: String, isCompleted: Boolean) {
        val uid = requireUserId()
        val docId = "${dateString}_$prayerType"
        val docRef = db.collection("users").document(uid).collection("prayer_logs").document(docId)
        try {
            val existing = docRef.get().await()
            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "dateString" to dateString,
                "prayerType" to prayerType,
                "isCompleted" to isCompleted,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!existing.exists() || existing.get("createdAt") == null) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }
}
