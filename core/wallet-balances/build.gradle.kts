plugins {
    alias(libs.plugins.fortera.android.library)
}

android {
    namespace = "com.yasashny.fortera.core.walletbalances"
}

dependencies {
    api(projects.core.domainWallet)
    api(projects.core.domainCrypto)
    api(projects.core.common)
    implementation(libs.koin.androidx.compose)
}
