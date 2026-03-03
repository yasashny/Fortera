plugins {
    alias(libs.plugins.fortera.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.yasashny.fortera.feature.tokendetails.api"
}

dependencies {
    api(libs.kotlinx.serialization.json)
}
