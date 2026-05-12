plugins {
    alias(libs.plugins.fortera.android.library)
}

android {
    namespace = "com.yasashny.fortera.core.domaincrypto"
}

dependencies {
    api(projects.core.common)
    implementation(libs.koin.androidx.compose)
}
