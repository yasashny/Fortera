plugins {
    alias(libs.plugins.fortera.android.library)
    alias(libs.plugins.fortera.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.yasashny.fortera.core.navigation"
}

dependencies {
    api(libs.navigation3.runtime)
    api(libs.navigation3.ui)
    api(libs.kotlinx.serialization.json)
    implementation(libs.lifecycle.viewmodel.navigation3)
}
