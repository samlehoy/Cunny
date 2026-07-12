# Cunny — Google Play Store Listing & Console Guide

Dokumen ini berisi draf teks untuk listing aplikasi di Google Play Store (dalam Bahasa Indonesia dan Bahasa Inggris) serta panduan langkah-demi-langkah untuk mengisi kuesioner **Content Rating** dan **Data Safety** di Google Play Console.

---

## 📝 1. Store Listing Text Assets

### A. Versi Bahasa Indonesia (Utama)
*   **App Title (Nama Aplikasi)** (Maks 30 karakter):
    `Cunny — Belajar AI Interaktif`
*   **Short Description (Deskripsi Singkat)** (Maks 80 karakter):
    `Belajar konsep kecerdasan buatan (AI) lewat permainan & simulasi interaktif!`
*   **Full Description (Deskripsi Lengkap)** (Maks 4000 karakter):
    ```text
    Selamat datang di Cunny, aplikasi pembelajaran kecerdasan buatan (AI) interaktif pertama yang dirancang khusus untuk anak-anak dan remaja! 

    Di Cunny, kamu tidak hanya membaca teori. Kamu akan belajar lewat simulasi langsung ("Learn-by-Doing") untuk memahami cara kerja teknologi masa depan secara menyenangkan, tanpa perlu menulis kode pemrograman!

    🚀 FITUR UTAMA CUNNY:
    • 🤖 Playground Eksperimen AI: Cobalah simulasi interaktif untuk melihat langsung bagaimana komputer berpikir.
    • 📸 Pemindai Buah (Fruit Scanner): Pindai buah-buahan di sekitarmu menggunakan kamera dan lihat bagaimana model AI mendeteksi dan mengklasifikasikannya secara real-time.
    • 📦 Sorting Game: Pelajari perbedaan antara Supervised (belajar terarah) dan Unsupervised Learning (belajar mandiri) melalui game pengelompokan item yang seru.
    • 🧠 Neuron Sandbox: Atur bobot (weights) dan ambang batas (threshold) untuk memahami bagaimana sel saraf tiruan mengambil keputusan.
    • 💬 Asisten Pendamping Interaktif: Ditemani maskot robot ramah yang siap memandu dan bersuara menemanimu belajar di setiap langkah.
    • 🏆 Gamifikasi Seru: Kumpulkan XP, pertahankan nyala api Streak harianmu, naikkan level belajarmu, dan buka lencana (badge) keren!

    🔒 PRIVASI & KEAMANAN 100% OFFLINE-SAFE:
    Seluruh pemrosesan kecerdasan buatan dan klasifikasi gambar pada modul kamera dilakukan secara lokal di perangkat Anda (on-device ML). Cunny tidak mengirimkan foto atau data kamera Anda ke server mana pun, sehingga aman untuk privasi anak-anak.

    Cunny dirancang berdasarkan riset pedagogi edukasi AI K-12 internasional untuk menumbuhkan cara berpikir kritis, memecahkan masalah, dan memahami batasan serta etika kecerdasan buatan sejak dini.

    Ayo unduh Cunny sekarang dan jadilah penjelajah AI masa depan!
    ```

### B. Versi Bahasa Inggris (Terjemahan)
*   **App Title** (Max 30 chars):
    `Cunny — Learn AI Interactively`
*   **Short Description** (Max 80 chars):
    `Learn Artificial Intelligence (AI) concepts through games & simulations!`
*   **Full Description** (Max 4000 chars):
    ```text
    Welcome to Cunny, the first interactive Artificial Intelligence (AI) learning app designed specifically for kids and teens!

    With Cunny, you don't just read dry theories. You learn by doing through hands-on simulations to understand how future technology works in a fun way—without writing a single line of code!

    🚀 KEY FEATURES OF CUNNY:
    • 🤖 AI Experiment Playground: Try interactive simulations to see firsthand how computers think.
    • 📸 Fruit Scanner: Scan fruits around you using your camera and see how the AI model detects and classifies them in real-time.
    • 📦 Sorting Game: Master the difference between Supervised and Unsupervised Learning through engaging sorting mini-games.
    • 🧠 Neuron Sandbox: Tweak weights and thresholds to understand how artificial neural networks make logical decisions.
    • 💬 Friendly Mascot Assistant: Learn along with our friendly robot companion who speaks and guides you through every step.
    • 🏆 Fun Gamification: Earn XP, maintain your daily Streak, level up, and unlock awesome badges!

    🔒 100% PRIVACY & OFFLINE-SAFE:
    All machine learning and image classification processes on the camera module are executed locally on your device (on-device ML). Cunny never uploads your photos or camera data to any external server, ensuring child privacy.

    Cunny is crafted based on international K-12 AI education pedagogy research to foster critical thinking, problem-solving, and a responsible understanding of AI limitations and ethics from an early age.

    Download Cunny today and become a future AI explorer!
    ```

---

## 🛡️ 2. Panduan Pengisian Data Safety (Data Safety Form)

Saat mengisi form **Data Safety** di Google Play Console, deklarasikan jawaban berikut:

1.  **Apakah aplikasi Anda mengumpulkan atau membagikan tipe data pengguna yang wajib dideklarasikan?**
    *   Jawaban: **Ya**
2.  **Apakah semua data pengguna yang dikumpulkan oleh aplikasi Anda dienkripsi saat transit?**
    *   Jawaban: **Ya** (Semua API menggunakan protokol HTTPS aman)
3.  **Apakah Anda menyediakan metode bagi pengguna untuk meminta penghapusan data mereka?**
    *   Jawaban: **Ya** (Aplikasi memiliki opsi "Hapus Akun" langsung di dalam tab Profile/Settings)
4.  **Tipe data apa saja yang dikumpulkan?**
    *   *Informasi Pribadi:*
        *   **Nama** (Dikumpulkan untuk profil akun, dienkripsi saat transit, dapat dihapus)
        *   **Alamat Email** (Dikumpulkan untuk Firebase Authentication, dienkripsi saat transit, dapat dihapus)
    *   *Foto dan Video (Kamera):*
        *   **Foto** (Digunakan secara opsional oleh fitur kamera Fruit Scanner. **PENTING:** Nyatakan bahwa foto *hanya diproses secara lokal di perangkat* (on-device) dan *tidak disimpan secara permanen* serta *tidak dikirimkan ke server*).
5.  **Tujuan penggunaan data:**
    *   *Nama & Email:* **Fungsionalitas Aplikasi (App Functionality)** dan **Manajemen Akun (Account Management)**.

---

## 🔒 3. Panduan Pengisian Content Rating Questionnaire

Saat mengisi kuesioner **Content Rating** untuk mendapatkan rating **Everyone / PEGI 3**:

1.  **Kategori Aplikasi:** Pilih **Edukasi / Sosial (Education / Social)** atau **Utilitas Umum**.
2.  **Kekerasan (Violence):** Apakah aplikasi berisi adegan kekerasan? -> **Tidak**
3.  **Ketakutan (Fear):** Apakah aplikasi berisi gambar/suara yang menakutkan? -> **Tidak**
4.  **Seksualitas (Sexuality):** Apakah aplikasi menampilkan ketelanjangan/konten seksual? -> **Tidak**
5.  **Bahasa Kasar (Crude Humor/Language):** Apakah berisi kata kotor/humor kasar? -> **Tidak**
6.  **Zat Adiktif (Substances):** Apakah merujuk pada narkoba, rokok, atau alkohol? -> **Tidak**
7.  **Interaksi Pengguna:**
    *   Apakah pengguna dapat berinteraksi/bertukar pesan dengan pengguna lain? -> **Tidak** (Cunny bersifat single-player, data tersinkronisasi hanya ke cloud akun pribadi).
    *   Apakah aplikasi membagikan lokasi fisik pengguna? -> **Tidak**
    *   Apakah aplikasi memungkinkan pembelian digital? -> **Tidak** (Fitur billing telah dinonaktifkan di versi v1.0).

---

## 🏆 4. Target Penonton & Program Keluarga (Target Audience)

*   **Rentang Usia (Age Groups):** Pilih rentang usia **13-15 tahun** dan **16-17 tahun** (dan 18+ jika ingin mencakup seluruh pengguna).
*   **Apakah aplikasi secara tidak sengaja menarik perhatian anak-anak di bawah 13 tahun?**
    *   Jawaban: **Tidak** (Berdasarkan panduan COPPA di `docs/legal/coppa-evaluation.md`, target utama adalah remaja SMP/K-12 usia 13 ke atas, dan aplikasi tidak dipasarkan untuk anak usia dini). Hal ini menghindarkan aplikasi dari regulasi ketat Families Program Google Play yang lebih rumit.
