plugins {
    alias(libs.plugins.fortera.android.library)
    alias(libs.plugins.fortera.android.library.compose)
}

android {
    namespace = "com.yasashny.fortera.core.ui"
}

dependencies {
    api(projects.core.common)
    api(projects.core.designsystem)
    api(projects.core.domainCrypto)

    // Coil for image loading
    api(libs.coil.compose)
    api(libs.coil.network.okhttp)
}
