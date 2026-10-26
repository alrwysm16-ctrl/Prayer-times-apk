const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-e8dcc2f3-d623-4603-abe1-033d7cda7f5b";

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      databaseId: DATABASE_ID,
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write settings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const settingsRef = unauthDb.collection("users").doc("user_123").collection("settings").doc("current");

  await assertFails(settingsRef.get());
  await assertFails(settingsRef.set({
    userId: "user_123",
    cityName: "الرياض",
    methodId: "umm_al_qura",
    madhhabId: "shafi",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Authenticated user can read and write their own settings", async () => {
  const authDb = testEnv.authenticatedContext("user_123").firestore();
  const settingsRef = authDb.collection("users").doc("user_123").collection("settings").doc("current");

  await assertSucceeds(settingsRef.set({
    userId: "user_123",
    cityName: "الرياض",
    methodId: "umm_al_qura",
    madhhabId: "shafi",
    useAutoLocation: true,
    countryName: "المملكة العربية السعودية",
    latitude: 24.7136,
    longitude: 46.6753,
    timezoneHours: 3.0,
    voiceId: "makkah",
    fajrVoiceId: "fajr_makkah",
    volume: 0.8,
    isDarkTheme: false,
    createdAt: new Date(),
    updatedAt: new Date()
  }));

  await assertSucceeds(settingsRef.get());
});

test("User cannot read or write another user's settings", async () => {
  const user1Db = testEnv.authenticatedContext("user_1").firestore();
  const user2SettingsRef = user1Db.collection("users").doc("user_2").collection("settings").doc("current");

  await assertFails(user2SettingsRef.get());
  await assertFails(user2SettingsRef.set({
    userId: "user_2",
    cityName: "مكة المكرمة",
    methodId: "umm_al_qura",
    madhhabId: "shafi",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("User can record and read their prayer logs", async () => {
  const authDb = testEnv.authenticatedContext("user_123").firestore();
  const logRef = authDb.collection("users").doc("user_123").collection("prayer_logs").doc("log_fajr_today");

  await assertSucceeds(logRef.set({
    userId: "user_123",
    dateString: "2026-10-25",
    prayerType: "fajr",
    isCompleted: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }));

  await assertSucceeds(logRef.get());
});
