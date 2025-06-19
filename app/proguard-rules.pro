# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Support library
-keep public class androidx.multidex.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity

# Coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# Compose
-keep class androidx.compose.runtime.** { *; }