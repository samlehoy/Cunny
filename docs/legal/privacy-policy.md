# Privacy Policy — Cunny: Belajar AI Interaktif

**Effective Date:** July 12, 2026  
**Last Updated:** July 12, 2026  
**Developer:** Eleonore Z  
**Package:** `com.eleonorez.cunny`  
**Contact:** cunny.app.edu@gmail.com

---

## 1. Introduction

Welcome to **Cunny — Belajar AI Interaktif** ("Cunny", "the App", "we", "us", or "our"). This Privacy Policy explains how we collect, use, store, and protect your personal information when you use our mobile application.

Cunny is an interactive AI education app designed for K-12 students (ages 11–14 / SMP) in Indonesia. We are committed to protecting your privacy and complying with applicable data protection laws, including Indonesia's **UU PDP (Undang-Undang Pelindungan Data Pribadi)** No. 27/2022 and, where applicable, the U.S. **Children's Online Privacy Protection Act (COPPA)**.

> **Bahasa Indonesia:**  
> Selamat datang di **Cunny — Belajar AI Interaktif**. Kebijakan Privasi ini menjelaskan bagaimana kami mengumpulkan, menggunakan, menyimpan, dan melindungi informasi pribadi Anda saat menggunakan aplikasi kami. Cunny adalah aplikasi edukasi AI interaktif yang dirancang untuk siswa K-12 (usia 11–14 tahun / SMP) di Indonesia.

---

## 2. Information We Collect

### 2.1 Account Information (Required)

When you create an account or sign in with Google, we collect:

| Data | Source | Purpose |
|------|--------|---------|
| **Email address** | Firebase Authentication (Google Sign-In) | Account identification, authentication |
| **Display name** | Firebase Authentication (Google Sign-In) | Personalization, in-app greeting |
| **User ID (UID)** | Firebase Authentication | Unique account identifier |

### 2.2 Learning Progress Data (Automatic)

As you use the App, we automatically collect and store your learning progress:

| Data | Purpose |
|------|---------|
| **XP (Experience Points)** | Track learning achievements |
| **Level** | Reflect learning progress |
| **Streak count** | Daily learning consistency |
| **Energy** | Session management (freemium model) |
| **Badges earned** | Achievement recognition |
| **Bookmarked lessons** | Personalized content access |
| **Course/lesson completion status** | Progress tracking |
| **Quiz answers and scores** | Learning assessment |

### 2.3 Photos and Videos (Optional)

The App includes a **Fruit Scanner** feature that uses your device camera for on-device AI classification:

- **Camera access** is requested only when you use the Fruit Scanner feature
- **Photos are processed entirely on-device** using a bundled TensorFlow Lite model
- **No photos or videos are uploaded to our servers** or any third-party service
- Camera permission can be revoked at any time through your device settings

### 2.4 Device and Technical Information

We may collect limited technical information for app stability:

- Device model and OS version (via Firebase Crashlytics, when enabled)
- App version
- Crash logs and performance data

> **Bahasa Indonesia:**  
> **Data yang kami kumpulkan:** Email dan nama tampilan (dari Google Sign-In), data progres belajar (XP, level, streak, energi, badge, bookmark), dan data teknis perangkat untuk stabilitas aplikasi. Fitur Fruit Scanner memproses foto **sepenuhnya di perangkat** — tidak ada foto yang diunggah ke server kami.

---

## 3. Information We Do NOT Collect

We want to be transparent about what we **do not** collect:

- ❌ **Location data** (GPS, network-based, or approximate)
- ❌ **Contacts or address book**
- ❌ **SMS, call logs, or messages**
- ❌ **Financial or payment information** (handled entirely by Google Play)
- ❌ **Browsing history**
- ❌ **Advertising identifiers for behavioral advertising**
- ❌ **Biometric data**

---

## 4. How We Use Your Information

We use the collected information for the following purposes:

1. **Authentication & Account Management** — To create, maintain, and secure your account
2. **Learning Progress** — To track and display your educational progress, achievements, and streaks
3. **Personalization** — To greet you by name and show relevant learning content
4. **App Improvement** — To identify and fix crashes, improve performance and user experience
5. **Energy System** — To manage the freemium energy-based access model
6. **Sync Across Sessions** — To restore your progress when you sign in on a new device

We do **NOT** use your data for:

- ❌ Behavioral advertising or ad targeting
- ❌ Selling or renting to third parties
- ❌ Profiling for purposes unrelated to education
- ❌ Automated decision-making with legal effects

> **Bahasa Indonesia:**  
> Kami menggunakan data Anda **hanya** untuk autentikasi, pelacakan progres belajar, personalisasi, dan peningkatan aplikasi. Kami **TIDAK** menggunakan data Anda untuk iklan, tidak menjualnya, dan tidak membagikannya ke pihak ketiga.

---

## 5. Data Storage and Security

### 5.1 Where Your Data Is Stored

| Data Type | Storage Location | Provider |
|-----------|-----------------|----------|
| Account credentials (email, name, UID) | Firebase Authentication | Google (Firebase) |
| Learning progress (XP, level, streaks, badges, bookmarks) | PostgreSQL database | Cloudflare Workers (REST API) |
| Cached learning data, offline progress | Room (SQLite) database | On-device (local) |

### 5.2 Security Measures

- **Encryption in Transit:** All data transmitted between the App and our servers uses **HTTPS/TLS** encryption
- **Authentication:** API requests are authenticated using Firebase Auth tokens
- **On-Device Processing:** Camera/ML features process data locally; no images leave your device
- **Secure Infrastructure:** Our backend runs on Cloudflare Workers with built-in DDoS protection and edge security

### 5.3 Data Locations

- Firebase Authentication servers are operated by Google and may store data in Google's global infrastructure
- Our API backend runs on Cloudflare's global edge network
- Local data remains on your device

---

## 6. Data Sharing and Third-Party Services

### 6.1 We Do NOT Sell Your Data

**We do not sell, rent, lease, or trade your personal information to any third party.**

### 6.2 Third-Party SDKs

The App uses the following third-party services:

| SDK | Purpose | Data Shared | Privacy Policy |
|-----|---------|-------------|----------------|
| **Firebase Authentication** | User sign-in (Google Sign-In) | Email, display name, UID | [Firebase Privacy](https://firebase.google.com/support/privacy) |
| **Firebase Crashlytics** *(planned)* | Crash reporting & stability | Device info, crash logs | [Firebase Privacy](https://firebase.google.com/support/privacy) |
| **Google Play Billing** *(planned)* | Subscription management | Transaction data (handled by Google) | [Google Privacy](https://policies.google.com/privacy) |
| **TensorFlow Lite** | On-device ML inference | None (runs locally) | [TensorFlow Privacy](https://www.tensorflow.org/about/privacy) |

### 6.3 Legal Disclosure

We may disclose your information if required by law, regulation, legal process, or governmental request.

> **Bahasa Indonesia:**  
> Kami **TIDAK menjual** data Anda. SDK pihak ketiga yang kami gunakan (Firebase, Google Play) memiliki kebijakan privasi masing-masing. Model AI berjalan sepenuhnya di perangkat Anda.

---

## 7. Children's Privacy

### 7.1 Target Audience

Cunny is designed for students aged **11–14 years** (SMP / junior high school level in Indonesia). We take children's privacy seriously.

### 7.2 Safeguards for Young Users

- **No behavioral advertising:** We do not display ads or use data for ad targeting
- **No personal data sold:** We never sell children's personal information
- **Minimal data collection:** We only collect what is necessary for the app to function (authentication and learning progress)
- **No social features:** The app does not include chat, messaging, user-generated content sharing, or social networking features
- **No location tracking:** We do not collect or use location data
- **On-device ML:** The Fruit Scanner AI model runs entirely on-device; no photos are transmitted

### 7.3 Parental Rights

Parents or legal guardians of users under 18 may:

- **Request access** to their child's personal data
- **Request deletion** of their child's account and data
- **Withdraw consent** for data processing

To exercise these rights, contact us at **cunny.app.edu@gmail.com**.

### 7.4 COPPA Compliance Note

For users in the United States under 13 years of age, we comply with the Children's Online Privacy Protection Act (COPPA). We use Google Sign-In as the authentication method, which requires users to have a Google account (subject to Google's own age verification and family controls). We do not knowingly collect personal information from children under 13 without verifiable parental consent facilitated through Google's Family Link or equivalent parental controls.

> **Bahasa Indonesia:**  
> Cunny dirancang untuk siswa usia 11–14 tahun. Kami **tidak menampilkan iklan**, **tidak menjual data anak**, dan hanya mengumpulkan data yang diperlukan. Orang tua dapat meminta akses atau penghapusan data anak mereka dengan menghubungi kami.

---

## 8. Your Rights and Choices

### 8.1 Access and Portability

You can view your learning progress data within the App at any time (Profile screen).

### 8.2 Account Deletion

You can delete your account and all associated data:

- **In-App:** Go to **Settings → Delete Account**
- **Via API:** `DELETE /auth/me` endpoint
- **Via Email:** Contact cunny.app.edu@gmail.com

Upon account deletion:
- Your Firebase Authentication record will be deleted
- Your learning progress data will be permanently removed from our servers
- Cached data on your device will be cleared

### 8.3 Camera Permission

You can revoke camera permission at any time through your device's Settings app. The Fruit Scanner feature will be unavailable without camera permission, but all other app features will continue to work.

### 8.4 Google Account Controls

Since authentication is via Google Sign-In, you can also manage your connected apps through your [Google Account settings](https://myaccount.google.com/permissions).

> **Bahasa Indonesia:**  
> Anda berhak mengakses, menghapus, dan mengontrol data Anda. Penghapusan akun dapat dilakukan melalui **Pengaturan → Hapus Akun** di dalam aplikasi, atau hubungi kami via email.

---

## 9. Data Retention

- **Active accounts:** Data is retained as long as your account is active
- **Deleted accounts:** All personal data is permanently deleted within **30 days** of account deletion request
- **Crash logs:** Retained for up to **90 days** for stability analysis, then automatically purged
- **Local cache:** Cleared when the app is uninstalled or account is deleted

---

## 10. Changes to This Privacy Policy

We may update this Privacy Policy from time to time. When we make material changes:

- We will update the "Last Updated" date at the top of this document
- We will notify users through an in-app notice
- Continued use of the App after changes constitutes acceptance of the revised policy

We encourage you to review this Privacy Policy periodically.

---

## 11. Contact Us

If you have questions, concerns, or requests regarding this Privacy Policy or your personal data, please contact us:

- **Email:** cunny.app.edu@gmail.com
- **Developer:** Eleonore Z
- **App:** Cunny — Belajar AI Interaktif (`com.eleonorez.cunny`)

We will respond to all legitimate requests within **30 days**.

---

## 12. Summary Table

| Question | Answer |
|----------|--------|
| What data do you collect? | Email, display name, learning progress, device info |
| Do you collect location? | No |
| Do you collect contacts? | No |
| Do you share data with third parties? | No |
| Do you sell data? | No |
| Do you show ads? | No |
| Is data encrypted? | Yes (HTTPS/TLS) |
| Can I delete my data? | Yes (Settings → Delete Account) |
| Who do I contact? | cunny.app.edu@gmail.com |

---

*© 2026 Eleonore Z. All rights reserved.*
