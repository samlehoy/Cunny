# Cunny

Aplikasi Android untuk belajar AI secara interaktif. Ditujukan untuk pelajar K-12 yang ingin memahami konsep kecerdasan buatan melalui simulasi langsung — bukan sekadar membaca teori.

## Apa yang bisa dilakukan

- **Pelajaran interaktif** — materi AI dari API, ditampilkan sebagai teks naratif + widget sandbox yang bisa dimainkan langsung
- **19 widget sandbox** — simulasi hands-on: melatih neuron, mengelompokkan data, mengevaluasi prompt, mendeteksi bias, dll
- **Fruit Scanner** — scan buah pakai kamera/gallery, model TFLite on-device mengenali jenisnya dan menjelaskan alasannya
- **Gamifikasi** — XP, streak harian, badge (Explorer Bronze → Completion Gold)
- **Offline-first** — cache OkHttp + Room database, sync otomatis via WorkManager

## Tech stack

| Layer | Stack |
|-------|-------|
| UI | Kotlin, Jetpack Compose, Material3 |
| Navigation | Navigation Compose |
| Networking | Retrofit + OkHttp cache |
| Local DB | Room + DataStore |
| Auth | Firebase Auth + Credential Manager |
| ML | LiteRT (TensorFlow Lite) on-device |
| Animation | Lottie Compose |
| Background | WorkManager |
| Billing | Google Play Billing |
| Monitoring | Firebase Crashlytics |

## Struktur project

```
Cunny/                     App Android (Jetpack Compose)
├── data/                  Repositories, Retrofit, Room, sync
├── ml/                    FruitClassifier, ExplainabilityEngine
├── ui/compose/
│   ├── components/        GlassSurface, BottomNavBar, Confetti, dll
│   ├── screens/           14 screens (home, lesson, practice, profile, dll)
│   └── theme/             Color, Type, Dimens
└── helper/                GamificationManager, SoundSynthesizer

cunny-lessons-api/         Content API (Cloudflare Workers)
docs/
├── legal/                 Privacy policy, ToS, evaluasi COPPA
└── Journal/               Referensi jurnal akademik
```

## Setup lokal

**Prasyarat:** Android Studio, JDK 17+, Android SDK 36

```bash
# Clone
git clone https://github.com/samlehoy/Cunny.git
cd Cunny/Cunny

# Buat local.properties (isi credential sendiri)
cp local.properties.example local.properties

# Build
./gradlew assembleDebug

# Test
./gradlew testDebugUnitTest
```

`local.properties` perlu diisi:
- `WEB_CLIENT_ID` — Firebase Web Client ID untuk Google Sign-In
- `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` — signing config (hanya untuk release build)

## Content API

API untuk konten pelajaran di-host di Cloudflare Workers. Source code ada di folder `cunny-lessons-api/` dan juga di branch [`cunny-lessons-api`](https://github.com/samlehoy/Cunny/tree/cunny-lessons-api).

Live endpoint: `https://cunny-content-api.muttaqien0111.workers.dev/`

## Widget sandbox

Setiap pelajaran bisa mengandung widget interaktif. Beberapa contoh:

| Widget | Konsep AI |
|--------|-----------|
| Sorting Game | Supervised vs unsupervised learning |
| Neuron Sandbox | Cara kerja neuron dan LTU |
| Reward Trainer | Reinforcement learning |
| Next-Word Predictor | Language model sederhana |
| Bias Game | Bias dalam dataset |
| Diffusion Sandbox | Generative AI / diffusion |
| Privacy Auditor | Privasi dan etika AI |
| Spot The Fake | Deepfake detection |

## Legal

- [Privacy Policy](docs/legal/privacy-policy.md)
- [Terms of Service](docs/legal/terms-of-service.md)
- [COPPA Evaluation](docs/legal/coppa-evaluation.md)

## Lisensi

Hak cipta dilindungi. Kode ini dipublikasikan untuk keperluan akademik dan portofolio.
