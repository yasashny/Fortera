plugins {
    alias(libs.plugins.fortera.android.library)
}

android {
    namespace = "com.yasashny.fortera.core.common"
}

dependencies {
    implementation(libs.koin.androidx.compose)
    api(libs.kotlinx.coroutines.android)
}
