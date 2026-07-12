# ============================================================
# Cunny — ProGuard / R8 keep rules for release builds
# ============================================================

# Preserve line numbers for Crashlytics stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic signatures & annotations (needed by Retrofit, Gson, Room)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Exceptions

# ---- Retrofit ----
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep interface com.eleonorez.cunny.data.retrofit.ApiService { *; }

# ---- API response models (Gson-deserialized) ----
-keep class com.eleonorez.cunny.data.response.** { *; }
-keep class com.eleonorez.cunny.data.model.** { *; }

# ---- Gson / JSON serialization ----
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# ---- Room Database ----
-keep class * extends androidx.room.RoomDatabase
-keep class com.eleonorez.cunny.data.room.** { *; }
-keep class com.eleonorez.cunny.data.database.** { *; }
-dontwarn androidx.room.paging.**

# ---- Firebase (Auth, Crashlytics, Storage) ----
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ---- TensorFlow Lite / LiteRT ----
-keep class org.tensorflow.** { *; }
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**
-keep class com.google.ai.edge.** { *; }
-dontwarn com.google.ai.edge.**

# ---- Google Play Billing ----
-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ---- WorkManager / SyncWorker ----
-keep class com.eleonorez.cunny.data.sync.SyncWorker { *; }
-keep class com.eleonorez.cunny.data.sync.SyncManager { *; }

# ---- OkHttp ----
-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ---- Kotlin Coroutines ----
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ---- Google Credential Manager / Identity ----
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn com.google.android.libraries.identity.**
-keep class androidx.credentials.** { *; }

# ---- Glide ----
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# ---- Lottie ----
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# ---- DataStore ----
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**