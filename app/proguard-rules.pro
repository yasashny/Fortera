-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature,InnerClasses,EnclosingMethod,Exceptions
-keepattributes *Annotation*,RuntimeVisibleAnnotations,AnnotationDefault

-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

-dontnote kotlinx.serialization.**

-keepclassmembers @kotlinx.serialization.Serializable class **$$serializer {
    *** descriptor;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class org.web3j.abi.** { *; }
-keep class org.web3j.crypto.** { *; }
-dontwarn org.web3j.**

-keep class org.bitcoinj.crypto.** { *; }
-dontwarn org.bitcoinj.**
-dontwarn org.bitcoin.**

-dontwarn org.bouncycastle.**
-dontwarn javax.naming.**

-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.slf4j.**
-dontwarn org.conscrypt.**

-dontwarn com.google.common.**
-dontwarn com.google.errorprone.**
-dontwarn com.google.j2objc.**
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.**
-dontwarn java.lang.instrument.**
-dontwarn sun.misc.**

-dontwarn com.fasterxml.jackson.**
-dontwarn java.beans.**
-dontwarn io.reactivex.**
-dontwarn rx.**

-keep class com.tradingview.lightweightcharts.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-dontwarn com.tradingview.lightweightcharts.**
