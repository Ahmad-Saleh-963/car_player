# Optimize APK Size to < 5MB
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify

# Keep App Classes
-keep class com.example.my_car.** { *; }

# Suppress library warnings for R8
-dontwarn androidx.media3.**
-dontwarn coil.**
