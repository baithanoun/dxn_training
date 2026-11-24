# Keep attributes needed by Retrofit/Gson reflection.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# DTOs are (de)serialized by Gson via field names / @SerializedName.
-keep class com.dxn.training.data.remote.** { *; }
-keep class com.dxn.training.data.model.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Retrofit service interfaces.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Kotlin metadata used by reflection/coroutines.
-keep class kotlin.Metadata { *; }
