# Proguard rules for Supabase serialization and Google auth
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep class com.mcbdone.app.data.model.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
