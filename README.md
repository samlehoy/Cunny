# Cunny — Media Pembelajaran AI Interaktif (K-12)

[![Android Build Status](https://img.shields.io/badge/Build-Debug_Passing-success?style=flat-square&logo=android)](file:///f:/Project/Cunny/app-debug.apk)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9.22-purple?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![TFLite Version](https://img.shields.io/badge/LiteRT_/_TFLite-1.4.2-orange?style=flat-square)](https://ai.google.dev/edge/litert)

**Cunny** adalah aplikasi Android berbasis **Jetpack Compose** yang dirancang sebagai media pembelajaran kecerdasan buatan (AI) interaktif untuk siswa K-12. Aplikasi ini mengutamakan metode belajar berbasis praktik (*learning by doing*) melalui simulasi interaktif (*sandbox widgets*), pengenalan objek secara lokal (*on-device ML*), dan elemen gamifikasi yang menarik.

Seluruh visual aplikasi mengadopsi standar **3D Tactile Glassmorphism** yang dikalibrasi secara eksklusif dalam **Bahasa Indonesia (ID-First)**.

---

## 🚀 Fitur Utama

- **100% Bahasa Indonesia (ID-First)**: Semua modul pembelajaran, antarmuka kuis, teks penjelasan AI, dan navigasi dilokalisasi penuh ke Bahasa Indonesia agar ramah anak.
- **On-Device Fruit Scanner (ML)**: Pemindaian gambar buah langsung dari kamera/galeri secara offline menggunakan **LiteRT (TensorFlow Lite) 1.4.2** (~4MB model `fruit_classifier.tflite` berbasis MobileNet). AI memberikan deteksi kelas dan narasi penjelasan cerdas (*Rationale*) dalam Bahasa Indonesia.
- **19 Widget Sandbox Interaktif**: Simulasi kognitif visual, termasuk:
  - *Neuron Sandbox* (LTU & bobot bias)
  - *Sorting Game* (Supervised vs Unsupervised)
  - *Reward Trainer* (Reinforcement Learning)
  - *Next-Word Predictor* (Language Model)
  - *Diffusion & Prompt Evaluation* (Generative AI)
- **Sistem Desain 3D Taktil (Cunny Style)**: Antarmuka premium yang memadukan kaca buram (*glassmorphism*), elevasi 3D padat (`tactileShadow`), serta efek getaran pegas (*spring animations*) untuk navigasi yang taktil.
- **Sistem Gamifikasi**: Manajemen energi, akumulasi XP, pelacak streak harian, dan lencana pencapaian (Badges) interaktif.
- **Sinkronisasi Offline-First**: Caching jaringan berbasis Retrofit/OkHttp dan database lokal Room dengan sinkronisasi otomatis menggunakan Android `WorkManager`.

---

## 🛠️ Arsitektur Aplikasi

```mermaid
graph TD
    subgraph UI Layer (Jetpack Compose)
        A[MainActivity / NavHost] --> B[HomeScreen - Beranda]
        A --> C[CoursesScreen - Materi]
        A --> D[PlaygroundScreen]
        A --> E[ProfileScreen / Settings]
        A --> F[PredictionScreen - Hasil Scanner]
    end

    subgraph Business & ML Logic
        G[HomeViewModel] --> H[ProgressRepository]
        I[PracticeViewModel] --> J[FruitClassifier - LiteRT]
        I --> K[ExplainabilityEngine]
        L[CourseViewModel] --> M[CourseRepository]
    end

    subgraph Data & Cache Layer
        H --> N[(Room DB: Gamification)]
        H --> O[(DataStore: User Settings)]
        M --> P[Retrofit API Client]
        P --> Q[OkHttp Cache]
    end

    subgraph External & Cloud API
        P --> R[Cloudflare Workers API]
        A --> S[Firebase Auth]
        S --> T[Google Sign-In]
    end

    classDef ui fill:#E8E3FA,stroke:#6C5CE7,stroke-width:2px;
    classDef logic fill:#FFF3E0,stroke:#FF9800,stroke-width:2px;
    classDef data fill:#E8F5E9,stroke:#4CAF50,stroke-width:2px;
    classDef ext fill:#FFEBEE,stroke:#F44336,stroke-width:2px;

    class A,B,C,D,E,F ui;
    class G,I,J,K,L logic;
    class H,M,N,O,Q data;
    class P,R,S,T ext;
```

---

## 📦 Struktur Project

```
Cunny/
├── app-debug.apk                   <-- Build APK siap pakai (di Root)
├── Cunny/                         App Android (Kotlin + Jetpack Compose)
│   ├── app/src/main/assets/       Model ML (fruit_classifier.tflite) & data (fruit_labels.json)
│   ├── data/                      Repositories, Retrofit API service, Room DB, & Sync Manager
│   ├── ml/                        Classifier (LiteRT) & Explainability Engine (Penjelasan ID)
│   ├── ui/compose/
│   │   ├── components/            GlassSurface, 3D Buttons, ProgressTrack, dll
│   │   ├── screens/               14 Layar interaktif (Home, Course, Playground, dll)
│   │   └── theme/                 Token warna (gradien plum/nav), font Sora, & insets
│   └── helper/                    SoundSynthesizer (efek suara taktil) & GamificationManager
│
├── cunny-lessons-api/             Content API (Cloudflare Workers, endpoint materi & kuis)
├── docs/                          Spesifikasi hukum (Privacy, ToS), COPPA, & PRD
└── assets/                        Visual resource ilustrasi peta belajar (ai-path)
```

---

## 🛠️ Panduan Build Lokal

### Prasyarat
* Android Studio (Koala atau versi lebih baru)
* JDK 17+
* Android SDK 36 (Android 15)

### Langkah Pemasangan

1. **Clone Repositori**:
   ```bash
   git clone https://github.com/samlehoy/Cunny.git
   cd Cunny/Cunny
   ```

2. **Pengaturan Kredensial**:
   Salin berkas contoh konfigurasi lokal:
   ```bash
   cp local.properties.example local.properties
   ```
   Isi konfigurasi `local.properties` Anda dengan kredensial Firebase:
   * `WEB_CLIENT_ID` — Firebase Web Client ID untuk Google Sign-In.

3. **Kompilasi Debug APK**:
   ```powershell
   # Windows PowerShell / CMD
   .\gradlew.bat :app:assembleDebug
   ```
   Hasil build APK dapat ditemukan di `Cunny/app/build/outputs/apk/debug/app-debug.apk` or langsung di folder root repositori Anda (`app-debug.apk`).

4. **Menjalankan Unit Test**:
   ```powershell
   .\gradlew.bat :app:testDebugUnitTest
   ```

---

## 🎮 Kategori & Sandbox Widgets

Setiap jalur belajar memuat latihan sandbox yang dapat dimanipulasi secara visual:
* **Pengenalan AI (Dasar AI)**:
  - *AI vs Program Biasa*: Membedakan kompilasi kode aturan statis dan pola dinamis.
  - *AI Around Us*: Mengenal AI di perabotan rumah tangga.
* **Bagaimana AI Belajar**:
  - *Supervised vs Unsupervised*: Memisahkan buah berdasarkan petunjuk (*Sorting Game*).
  - *Reinforcement Learning*: Melatih robot melalui sistem hukuman & hadiah (*Reward Trainer*).
* **AI Generatif & Kreativitas**:
  - *Next-Word Predictor*: Menebak probabilitas kata teratas dari LLM.
  - *Diffusion Sandbox*: Rekonstruksi visual dari noise acak berbasis prompt.
* **Etika & Batasan AI**:
  - *Bias Game*: Mengenal bias dataset wajah dari sampel yang tidak seimbang.
  - *Deepfake Spotter*: Menganalisis perbedaan citra sintetik dan asli.

---

## 📑 Kebijakan Hukum & Evaluasi COPPA

Aplikasi ini memenuhi standar privasi ramah anak:
- **Privacy Policy**: [docs/legal/privacy-policy.md](docs/legal/privacy-policy.md)
- **Terms of Service**: [docs/legal/terms-of-service.md](docs/legal/terms-of-service.md)
- **COPPA Evaluation**: [docs/legal/coppa-evaluation.md](docs/legal/coppa-evaluation.md)

---

## 📄 Lisensi

Hak cipta dilindungi. Kode dan aset pada repositori ini dipublikasikan secara eksklusif untuk keperluan portofolio, akademik, dan evaluasi pengembang.
