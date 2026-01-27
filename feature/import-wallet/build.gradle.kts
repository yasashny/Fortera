plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.importwallet"
}

dependencies {
    implementation(projects.core.domainWallet)
    implementation(projects.feature.importWalletApi)
    implementation(projects.feature.mainApi)
}
