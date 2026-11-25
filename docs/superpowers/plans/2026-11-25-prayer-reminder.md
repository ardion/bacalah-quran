# Pengingat Waktu Sholat Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Notifikasi tepat waktu sholat (Android & iOS) dengan pilihan per sholat: mati, notifikasi, atau cuplikan azan.

**Architecture:** Domain menghasilkan daftar `ReminderEvent` dari jadwal beberapa hari (Kemenag/Aladhan, cache-first) dan pengaturan pengguna; `ReminderScheduler` per platform mendaftarkannya sebagai notifikasi lokal. Android memakai `AlarmManager` + receiver yang menjadwalkan ulang dirinya; iOS memakai `UNUserNotificationCenter` dengan jendela 60 notifikasi.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Koin 4.1, kotlinx-datetime 0.7.1, kotlinx-serialization, Ktor 3.5, androidx.core (NotificationCompat), UserNotifications (iOS).

**Spec:** `docs/superpowers/specs/2026-11-25-prayer-reminder-design.md`

## Global Constraints

- Ikuti `app/.claude/Agreement/SKILLS.MD`: tanpa `!!`, tanpa komentar (kecuali alasan `@Suppress`), `@SerialName` di semua properti `@Serializable`, parameter > 4 dibungkus data class, teks UI di `strings.xml`.
- Pengingat **mati secara default**; izin notifikasi diminta hanya saat pengguna mengaktifkan.
- Audio: satu cuplikan 27,5 detik; Android `res/raw/adzan_clip.m4a`, iOS `adzan_clip.caf` di bundle app.
- Atribusi tampil di kartu pengingat: `Suara azan: Aaqib Azeez, CC BY-SA 4.0`.
- Android: jendela 7 hari; iOS: maksimal 60 event (≈12 hari). Web & desktop: `isSupported = false`, kartu disembunyikan.
- Android memakai `SCHEDULE_EXACT_ALARM`; bila tidak diizinkan, pakai `setWindow` dan tampilkan peringatan.
- Mengikuti mode senyap/DND (lewat channel/notifikasi sistem, tanpa memutar audio sendiri).

## Review Focus

- Waktu sholat yang sudah lewat hari ini tidak boleh dijadwalkan (test di Task 1).
- Jadwal yang melewati pergantian bulan harus menggabungkan dua bulan (test di Task 3).
- Waktu `--:--` atau format `"04:03 (WIB)"` dari kalender Aladhan harus diparse/dilewati, bukan crash (test di Task 1 & 3).
- Lokasi belum pernah diketahui → tidak ada yang dijadwalkan dan jadwal lama dibatalkan (test di Task 4).
- Pengguna menonaktifkan pengingat → semua notifikasi terjadwal dibatalkan (test di Task 4).

---

### Task 1: Model pengingat dan pembuat event

**Files:**
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/domain/model/Reminder.kt`
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/domain/usecase/BuildReminderEventsUseCase.kt`
- Test: `app/shared/src/commonTest/kotlin/id/ardion/quran/domain/usecase/BuildReminderEventsUseCaseTest.kt`

**Interfaces:**
- Produces:
  - `enum class Prayer { FAJR, DHUHR, ASR, MAGHRIB, ISHA }`
  - `enum class ReminderMode { OFF, NOTIFICATION, ADZAN }`
  - `data class ReminderSettings(val isEnabled: Boolean = false, val modes: Map<Prayer, ReminderMode> = Prayer.entries.associateWith { ReminderMode.NOTIFICATION })` + `fun modeFor(prayer: Prayer): ReminderMode`
  - `data class ReminderEvent(val id: Int, val prayer: Prayer, val time: LocalDateTime, val mode: ReminderMode, val locationName: String?)`
  - `fun PrayerTimes.timeOf(prayer: Prayer): String`
  - `class BuildReminderEventsUseCase { operator fun invoke(schedule: List<PrayerTimes>, settings: ReminderSettings, now: LocalDateTime, maxEvents: Int): List<ReminderEvent> }`

- [ ] **Step 1: Write failing tests** `BuildReminderEventsUseCaseTest`:
  - `disabledSettingsProduceNothing` → `isEnabled = false` ⇒ `emptyList()`
  - `offPrayersAreSkipped` → Dzuhur `OFF` ⇒ tidak ada event `DHUHR`
  - `pastTimesAreSkipped` → now `2026-11-25T12:00` ⇒ event pertama `ASR 15:07`
  - `eventsAreChronologicalAndIdsSequential` → dua hari ⇒ urut waktu, `id` = 0,1,2…
  - `maxEventsIsRespected` → `maxEvents = 3` ⇒ 3 event
  - `unparseableTimesAreSkipped` → `"--:--"` dilewati, `"04:03 (WIB)"` diparse 04:03
- [ ] **Step 2:** `./gradlew :app:shared:jvmTest --tests '*BuildReminderEventsUseCaseTest'` → FAIL (unresolved).
- [ ] **Step 3:** Implement. Parse `HH:mm` dari 5 karakter pertama via `toIntOrNull`; gabung dengan `PrayerTimes.date` menjadi `LocalDateTime`.
- [ ] **Step 4:** Run lagi → PASS.

### Task 2: Penyimpanan pengaturan dan lokasi terakhir

**Files:**
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/domain/repository/ReminderRepository.kt`
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/data/repository/ReminderRepositoryImpl.kt`
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/data/local/ReminderEntity.kt`
- Test: `app/shared/src/commonTest/kotlin/id/ardion/quran/data/repository/ReminderRepositoryImplTest.kt`

**Interfaces:**
- Consumes: `ReminderSettings`, `Prayer`, `ReminderMode` (Task 1); `KeyValueStore`; `LocationCoordinates`.
- Produces:
  - `data class ReminderLocation(val coordinates: LocationCoordinates, val isLive: Boolean)` (di `Reminder.kt`)
  - `interface ReminderRepository { val settings: StateFlow<ReminderSettings>; fun saveSettings(settings: ReminderSettings); fun lastLocation(): ReminderLocation?; fun saveLastLocation(location: ReminderLocation) }`
  - `class ReminderRepositoryImpl(store: KeyValueStore) : ReminderRepository`

- [ ] **Step 1: Failing tests:** `defaultsAreDisabledWithNotificationModes`, `settingsSurviveRestart`, `unknownModeNameFallsBackToNotification`, `corruptJsonFallsBackToDefaults`, `lastLocationRoundTrips`.
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3:** Implement. Key `key_reminder_settings`, `key_reminder_location`; entity menyimpan mode sebagai nama enum (`Map<String,String>`), dibaca dengan `ReminderMode.entries.firstOrNull { it.name == value } ?: NOTIFICATION`.
- [ ] **Step 4:** Run → PASS.

### Task 3: Jadwal beberapa hari (Kemenag + kalender Aladhan)

**Files:**
- Modify: `app/shared/src/commonMain/kotlin/id/ardion/quran/domain/repository/PrayerRepository.kt`
- Modify: `app/shared/src/commonMain/kotlin/id/ardion/quran/data/repository/PrayerRepositoryImpl.kt`
- Modify: `app/shared/src/commonMain/kotlin/id/ardion/quran/data/remote/PrayerApiService.kt`
- Modify: `app/shared/src/commonMain/kotlin/id/ardion/quran/data/response/PrayerResponseDto.kt`
- Modify: `app/shared/src/commonMain/kotlin/id/ardion/quran/data/mapper/PrayerMapper.kt`
- Modify: `core/src/commonMain/kotlin/id/ardion/quran/config/ApiConfig.kt` (`PRAYER_CALENDAR_URL = "https://api.aladhan.com/v1/calendar"`)
- Test: `app/shared/src/commonTest/kotlin/id/ardion/quran/data/repository/PrayerRepositoryImplTest.kt`

**Interfaces:**
- Produces: `suspend fun PrayerRepository.getSchedule(location: LocationCoordinates, isLiveLocation: Boolean, from: LocalDate, days: Int): Result<List<PrayerTimes>>`
  - Kemenag: cache bulanan yang sudah ada, untuk tiap bulan yang dicakup.
  - Bulan yang tidak tersedia dari Kemenag: `GET {PRAYER_CALENDAR_URL}/{year}/{month}?latitude&longitude&method=20&tune=…`, di-cache dengan key `aladhan_calendar_{lat}_{lon}_{year}_{month}` (grid 0,01°).
  - DTO baru `AladhanCalendarResponseDto(data: List<AladhanCalendarDayDto>)`, `AladhanCalendarDayDto(timings: TimingsDto?, date: AladhanDateDto?)`, `AladhanDateDto(gregorian: AladhanGregorianDto?)`, `AladhanGregorianDto(date: String? /* dd-MM-yyyy */)`.
  - Waktu dinormalkan ke `HH:mm` (`substringBefore(" ")`) di mapper.

- [ ] **Step 1: Failing tests:**
  - `scheduleSpanningMonthEndMergesTwoMonths` → from `2026-11-28`, days 5 ⇒ 5 hari, tanggal berurutan, request `/jadwal/1222/2026/11` dan `/2026/12`
  - `scheduleFallsBackToAladhanCalendarOutsideIndonesia` ⇒ source `CALCULATED`, request ke `/calendar/2026/11` dengan `tune`
  - `calendarTimesWithTimezoneSuffixAreNormalised` ⇒ `fajr == "04:03"`
  - `scheduleWorksOfflineFromCache`
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3:** Implement `getSchedule` memakai `resolvePlace`, `kemenagCityId`, `monthlySchedule` yang sudah ada; tambah `calendarMonth(location, year, month)`.
- [ ] **Step 4:** Run seluruh `PrayerRepositoryImplTest` → PASS.

### Task 4: Penjadwal (interface) dan use case penjadwalan

**Files:**
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/domain/repository/ReminderScheduler.kt`
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/domain/usecase/ScheduleRemindersUseCase.kt`
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/domain/time/NowProvider.kt`
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/platform/UnavailableReminderScheduler.kt`
- Modify: `SystemTodayProvider.kt` → juga implement `NowProvider`
- Modify: `di/QuranModule.kt`, `di/PlatformModule.*.kt` (jvm, js, wasmJs → `UnavailableReminderScheduler`)
- Test: `app/shared/src/commonTest/kotlin/id/ardion/quran/domain/usecase/ScheduleRemindersUseCaseTest.kt`

**Interfaces:**
- Produces:
  - `interface ReminderScheduler { val isSupported: Boolean; val maxEvents: Int; val windowDays: Int; val canScheduleExact: Boolean; suspend fun schedule(events: List<ReminderEvent>); fun cancelAll(); fun openExactAlarmSettings() }`
  - `fun interface NowProvider { fun now(): LocalDateTime }`
  - `class ScheduleRemindersUseCase(repository: ReminderRepository, prayerRepository: PrayerRepository, scheduler: ReminderScheduler, buildEvents: BuildReminderEventsUseCase, nowProvider: NowProvider) { suspend operator fun invoke() }`
    - tidak didukung → return; nonaktif atau lokasi null → `cancelAll()`; selain itu `getSchedule(from = now.date, days = windowDays)` → `buildEvents(…, maxEvents)` → `cancelAll()` lalu `schedule(events)`. Gagal ambil jadwal → biarkan jadwal lama.

- [ ] **Step 1: Failing tests** dengan `FakeReminderScheduler`: `disabledCancelsEverything`, `missingLocationCancelsEverything`, `enabledSchedulesEventsWithinWindow`, `scheduleFailureKeepsExistingReminders`, `unsupportedPlatformDoesNothing`.
- [ ] **Step 2:** Run → FAIL.
- [ ] **Step 3:** Implement + DI (`factoryOf(::BuildReminderEventsUseCase)`, `factoryOf(::ScheduleRemindersUseCase)`, `singleOf(::ReminderRepositoryImpl) bind ReminderRepository`, `single<NowProvider>`).
- [ ] **Step 4:** Run → PASS.

### Task 5: Penjadwal Android

**Files:**
- Create: `app/shared/src/androidMain/kotlin/id/ardion/quran/reminder/AndroidReminderScheduler.kt`
- Create: `app/shared/src/androidMain/kotlin/id/ardion/quran/reminder/ReminderNotifications.kt` (channel + builder)
- Create: `app/shared/src/androidMain/kotlin/id/ardion/quran/reminder/PrayerAlarmReceiver.kt`
- Create: `app/shared/src/androidMain/kotlin/id/ardion/quran/reminder/ReminderRescheduleReceiver.kt`
- Create: `app/shared/src/androidMain/res/raw/adzan_clip.m4a`, `app/shared/src/androidMain/res/drawable/ic_notification.xml`
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/presentation/reminder/ReminderMessage.kt` (`suspend fun ReminderEvent.toMessage(): ReminderMessage(title, body)` via `getString`)
- Modify: `app/androidApp/src/main/AndroidManifest.xml` (permissions `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`; receivers), `app/shared/build.gradle.kts` (`androidx-core-ktx`), `di/PlatformModule.android.kt`

**Interfaces:**
- Channel ID: `prayer_reminder` (suara default), `prayer_adzan` (suara `R.raw.adzan_clip`, usage `USAGE_NOTIFICATION`). `windowDays = 7`, `maxEvents = 64`, request code = `event.id`, `cancelAll()` membatalkan request code 0 until 64.
- Receiver alarm: tampilkan notifikasi (extra `title`, `body`, `mode`), lalu `goAsync()` dan panggil `ScheduleRemindersUseCase` lewat Koin. Receiver reschedule: `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`, `MY_PACKAGE_REPLACED`.
- Tap notifikasi → launch intent aplikasi.

- [ ] **Step 1:** Implement; `./gradlew :app:androidApp:assembleDevDebug :app:androidApp:lintDevDebug` → BUILD SUCCESSFUL, tanpa error lint.
- [ ] **Step 2: Manual (emulator):** set jam jadwal ke 1–2 menit ke depan lewat debug, kunci layar ⇒ notifikasi muncul tepat waktu; mode `ADZAN` memutar cuplikan; mode senyap ⇒ tanpa suara; `adb reboot` ⇒ jadwal terpasang ulang (`adb shell dumpsys alarm | grep id.ardion`).

### Task 6: Penjadwal iOS

**Files:**
- Create: `app/shared/src/iosMain/kotlin/id/ardion/quran/reminder/IosReminderScheduler.kt`
- Create: `app/iosApp/iosApp/adzan_clip.caf`
- Modify: `di/PlatformModule.ios.kt`

**Interfaces:** `maxEvents = 60`, `windowDays = 12`, `canScheduleExact = true`. `UNCalendarNotificationTrigger` (tahun–menit, tidak berulang), identifier `prayer_reminder_{id}`; `cancelAll()` menghapus semua pending dengan prefix itu. Mode `ADZAN` → `UNNotificationSound.soundNamed("adzan_clip.caf")`.

- [ ] **Step 1:** Implement; `./gradlew :app:shared:compileKotlinIosSimulatorArm64 :app:shared:compileKotlinIosArm64` → BUILD SUCCESSFUL.

### Task 7: UI pengaturan, izin, dan pemicu penjadwalan

**Files:**
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/presentation/reminder/ReminderSettingsViewModel.kt`
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/presentation/reminder/ReminderSettingsCard.kt`
- Create: `app/shared/src/commonMain/kotlin/id/ardion/quran/presentation/reminder/NotificationPermissionRequester.kt` + actual per platform
- Modify: `PrayerScreen.kt` (kartu di bawah daftar sholat), `PrayerViewModel.kt` (simpan `ReminderLocation` lalu panggil `ScheduleRemindersUseCase` setelah jadwal sukses), `HomeViewModel.kt` (jadwal ulang saat aplikasi dibuka), `strings.xml`, `QuranModule.kt`
- Test: `app/shared/src/commonTest/kotlin/id/ardion/quran/presentation/reminder/ReminderSettingsViewModelTest.kt`

**Interfaces:**
- `class ReminderSettingsViewModel(repository: ReminderRepository, scheduler: ReminderScheduler, scheduleReminders: ScheduleRemindersUseCase)`:
  `val state: StateFlow<ReminderUiState>` (`settings`, `isSupported`, `canScheduleExact`), `fun setEnabled(enabled: Boolean)`, `fun setMode(prayer: Prayer, mode: ReminderMode)`, `fun openExactAlarmSettings()`.
- `@Composable expect fun rememberNotificationPermissionRequester(): NotificationPermissionRequester` dengan `fun request(onResult: (Boolean) -> Unit)`; Android pakai `RequestPermission` (13+), iOS `requestAuthorizationWithOptions`, lainnya `onResult(false)`.
- Strings: `reminder_title`, `reminder_enable`, `reminder_mode_off`, `reminder_mode_notification`, `reminder_mode_adzan`, `reminder_permission_denied`, `reminder_exact_alarm_warning`, `reminder_adzan_credit` (`Suara azan: Aaqib Azeez, CC BY-SA 4.0`), `reminder_notification_title` (`Waktu %1$s`), `reminder_notification_body` (`%1$s · %2$s`).

- [ ] **Step 1: Failing tests:** `enablingSavesAndSchedules`, `changingModeReschedules`, `unsupportedPlatformReportsUnsupported`.
- [ ] **Step 2:** Run → FAIL. **Step 3:** Implement. **Step 4:** Run → PASS.

### Task 8: Verifikasi menyeluruh

- [ ] `./gradlew :core:jvmTest :app:shared:jvmTest :core:testAndroidHostTest :app:shared:testAndroidHostTest :app:androidApp:lintDevDebug` → semua lulus.
- [ ] Build `:app:androidApp:assembleProdRelease` (R8), desktop, web Wasm/JS, iOS → BUILD SUCCESSFUL.
- [ ] Uji manual Android (Task 5 Step 2) dan web (kartu tidak tampil).
- [ ] Perbarui `app/.claude/Architecture/SKILLS.MD` bagian status.
