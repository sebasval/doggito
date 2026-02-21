# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.example.doggitoapp.android.**$$serializer { *; }
-keepclassmembers class com.example.doggitoapp.android.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.doggitoapp.android.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Supabase / Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Coroutines
-dontwarn kotlinx.coroutines.**

# SLF4J
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Keep data classes used with Supabase postgrest
-keep class com.example.doggitoapp.android.data.repository.*Dto { *; }
-keep class com.example.doggitoapp.android.data.repository.*Dto$* { *; }
