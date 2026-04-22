plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.tokendetails"
}

dependencies {
    implementation(projects.core.domainWallet)
    implementation(projects.core.domainCrypto)
    implementation(projects.core.walletBalances)
    implementation(projects.feature.tokenDetailsApi)
    implementation(projects.feature.receiveApi)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.tradingview.lightweightcharts)
}
