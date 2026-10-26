package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.FirestoreUserSettings
import com.example.data.repository.NoorFirestoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NoorRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun authenticatedUser_canSaveAndReadSettings() = runBlocking {
    val uid = signInTestUser("user_test@example.com")
    val repository = NoorFirestoreRepository(firestore)

    val userSettings = FirestoreUserSettings(
      userId = uid,
      cityName = "الرياض",
      countryName = "المملكة العربية السعودية",
      methodId = "umm_al_qura",
      madhhabId = "shafi",
      volume = 0.8
    )

    repository.saveUserSettings(userSettings)
    val readSettings = repository.observeUserSettings().first()

    assertNotNull(readSettings)
    assertEquals(uid, readSettings?.userId)
    assertEquals("الرياض", readSettings?.cityName)
  }

  @Test
  fun authenticatedUser_canTogglePrayerLog() = runBlocking {
    val uid = signInTestUser("user_test2@example.com")
    val repository = NoorFirestoreRepository(firestore)

    repository.togglePrayerLog("2026-10-25", "fajr", true)
    val logs = repository.observePrayerLogs("2026-10-25").first()

    assertEquals(1, logs.size)
    assertEquals("fajr", logs[0].prayerType)
    assertEquals(true, logs[0].isCompleted)
  }
}
