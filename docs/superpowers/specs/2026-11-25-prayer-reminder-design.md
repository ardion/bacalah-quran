# Desain: Pengingat Waktu Sholat dengan Cuplikan Azan

Tanggal: 2026-11-25 · Status: menunggu persetujuan

## Tujuan

Aplikasi memunculkan notifikasi tepat saat masuk waktu sholat, walaupun aplikasi sedang
ditutup atau HP terkunci. Pengguna memilih per waktu sholat: mati, notifikasi biasa, atau
notifikasi dengan cuplikan azan.

## Sasaran

- Notifikasi tepat waktu sesuai jadwal Kemenag yang sudah dipakai layar Jadwal Sholat
  (fallback Aladhan + ihtiyath di luar Indonesia atau saat Kemenag tidak tersedia).
- Pengaturan per waktu sholat: `Mati` / `Notifikasi` / `Suara azan`.
- Suara azan berupa satu cuplikan 27,5 detik untuk semua waktu sholat. Lafaz khusus Subuh baru
  muncul menjelang akhir azan, jadi tidak ikut dalam cuplikan pembuka.
- Tetap berjalan tanpa internet selama jadwal bulan berjalan sudah tersimpan.
- Mengikuti mode senyap / Jangan Ganggu milik HP.

## Bukan sasaran (versi ini)

- Azan lengkap 3–4 menit (butuh foreground service di Android, tidak mungkin di iOS).
- Push notification dari server (FCM/APNs).
- Pengingat di web dan desktop.
- Pengingat sebelum waktu sholat (mis. "10 menit lagi"), imsak, atau tahajud.

## Anti-sasaran

Fitur dianggap gagal walaupun "jalan" bila:
- notifikasi muncul untuk kota yang salah, atau terlambat beberapa menit;
- suara berbunyi saat HP dalam mode senyap;
- izin notifikasi diminta saat aplikasi pertama dibuka tanpa konteks.

## Batasan platform

| | Android | iOS |
|---|---|---|
| Mekanisme | `AlarmManager.setExactAndAllowWhileIdle` + `BroadcastReceiver` | `UNUserNotificationCenter` |
| Batas jadwal | tidak ada batas praktis | maks. 64 notifikasi terjadwal |
| Jendela jadwal | 7 hari ke depan | 12 hari (60 notifikasi) |
| Suara | channel notifikasi per jenis suara (`res/raw/adzan_clip.m4a`, AAC) | `UNNotificationSound` `adzan_clip.caf` (IMA4) di bundle app |
| Izin | `POST_NOTIFICATIONS` (13+), `SCHEDULE_EXACT_ALARM` (12+) | izin notifikasi |
| Setelah restart | alarm hilang → dipasang ulang lewat `BOOT_COMPLETED` | tetap tersimpan sistem |

## Pendekatan yang dipertimbangkan

1. **Notifikasi lokal terjadwal (dipilih).** Aplikasi mendaftarkan alarm tiap waktu sholat ke
   sistem HP. Tepat waktu, tidak butuh server, bisa offline. Kekurangan: jadwal harus diperbarui
   berkala (iOS 64 notifikasi, Android setelah restart).
2. **Push dari server.** Butuh server, akun Firebase/APNs, dan data lokasi pengguna di server.
   Ditolak: biaya dan privasi, tanpa keuntungan ketepatan.
3. **Worker latar belakang yang mengecek tiap 15 menit.** Ditolak: WorkManager/BackgroundTasks
   tidak menjamin waktu, notifikasi bisa telat belasan menit.

## Desain

### Domain (`commonMain`)
- `ReminderMode { OFF, NOTIFICATION, ADZAN }` dan `Prayer { FAJR, DHUHR, ASR, MAGHRIB, ISHA }`.
- `ReminderSettings` (mode per sholat + `isEnabled`), disimpan lewat `KeyValueStore`.
- `PrayerRepository.getSchedule(location, from, days)` → jadwal beberapa hari, memakai cache
  Kemenag bulanan (bulan ini + bulan depan) atau kalender bulanan Aladhan sebagai fallback.
- `BuildReminderEventsUseCase`: logika murni yang mengubah jadwal + pengaturan menjadi daftar
  `ReminderEvent(prayer, time, mode, locationName)`; melewati waktu yang sudah lewat dan sholat
  yang `OFF`.
- `ScheduleRemindersUseCase`: ambil jadwal → bangun event → serahkan ke `ReminderScheduler`.

### Platform (`expect`/`actual` lewat `platformModule`)
- Interface `ReminderScheduler { isSupported; hasPermission(); requestPermission(); schedule(events); cancelAll() }`.
- Android: 2 channel (`Pengingat sholat`, `Azan`), receiver alarm yang memasang
  notifikasi lalu menjadwalkan ulang jendela berikutnya dari cache, receiver boot/ganti waktu/
  ganti zona waktu. Bila izin alarm tepat ditolak, jatuh ke `setWindow` dan UI memberi tahu.
- iOS: jadwalkan 60 notifikasi terdekat, suara `.caf` di bundle `iosApp`.
- Web, desktop: `UnavailableReminderScheduler` (`isSupported = false`), UI menyembunyikan menu.

### Kapan dijadwalkan ulang
Saat layar Jadwal Sholat memuat jadwal (lokasi bisa berubah), saat pengaturan diubah, saat HP
restart, saat jam/zona waktu berubah, dan (Android) setiap kali sebuah alarm berbunyi.

### UI
- Kartu "Pengingat sholat" di layar Jadwal Sholat: sakelar utama + pilihan per sholat.
- Pengingat **mati secara default**. Setelah jadwal pertama kali tampil, muncul ajakan
  "Aktifkan pengingat sholat?"; izin notifikasi diminta saat pengguna menyetujui.
- Bila izin ditolak: status jelas dan tombol ke pengaturan sistem.
- Ketuk notifikasi membuka aplikasi.

## Keputusan yang saya ambil (bisa dikoreksi)
- Pengaturan per waktu sholat, bukan satu sakelar.
- Web & desktop tidak didukung di versi ini.
- Pengingat mati sampai pengguna mengaktifkan setelah lokasi diketahui.
- `SCHEDULE_EXACT_ALARM` (izin dari pengguna) dipakai, bukan `USE_EXACT_ALARM`, karena kebijakan
  Google Play membatasi `USE_EXACT_ALARM` untuk aplikasi jam alarm/kalender.
- Ketuk notifikasi membuka aplikasi (belum langsung ke layar Jadwal Sholat).

## Audio azan
- Sumber: "The Adhan - Muslim Call to Prayer - Aaqib Azeez.mp3", Wikimedia Commons,
  lisensi CC BY-SA 4.0 (SHA1 `971bced4b0bf6067358afc78cac6e9d219226c19`).
- Cuplikan: detik 0–27,5 (berakhir di jeda setelah lafaz pertama), fade-out 1 detik, mono.
- Atribusi ditampilkan di kartu pengaturan pengingat: "Suara azan: Aaqib Azeez, CC BY-SA 4.0".
- Cuplikan turunan ikut berlisensi CC BY-SA 4.0; kode aplikasi tidak terpengaruh.

## Pengujian
- Unit test: `BuildReminderEventsUseCase` (sholat OFF dilewati, waktu lewat dilewati,
  pergantian hari/bulan, batas 60 event iOS), penyimpanan pengaturan,
  `getSchedule` lintas bulan dan fallback.
- Manual di emulator Android: notifikasi muncul tepat waktu dengan waktu jadwal yang dimajukan,
  setelah restart, dengan izin alarm ditolak, dan saat mode senyap.
- iOS hanya di-compile; uji di perangkat butuh Xcode aktif.
