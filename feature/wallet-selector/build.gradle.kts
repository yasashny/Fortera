plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.walletselector"
}

dependencies {
    implementation(projects.core.domainWallet)
    implementation(projects.feature.walletSelectorApi)
    implementation(projects.feature.createWalletApi)
    implementation(projects.feature.importWalletApi)
}
