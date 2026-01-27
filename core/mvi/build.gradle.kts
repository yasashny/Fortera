plugins {
    alias(libs.plugins.fortera.android.library)
    alias(libs.plugins.fortera.android.library.compose)
}

android {
    namespace = "com.yasashny.fortera.core.mvi"
}

dependencies {
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.kotlinx.coroutines.android)
    api(libs.koin.androidx.compose)
}
