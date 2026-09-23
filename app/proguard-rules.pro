# Optimize APK Size
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-repackageclasses ''
-allowaccessmodification

# Suppress library warnings for R8
-dontwarn androidx.media3.**
-dontwarn coil.**
