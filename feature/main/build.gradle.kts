plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.main"
}

dependencies {
    implementation(projects.core.domainWallet)
    implementation(projects.feature.mainApi)
    implementation(projects.feature.createWalletApi)
    implementation(projects.feature.importWalletApi)
    implementation(projects.feature.walletSelector)
}
