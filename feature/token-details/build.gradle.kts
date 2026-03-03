plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.tokendetails"
}

dependencies {
    implementation(projects.core.domainWallet)
    implementation(projects.core.domainCrypto)
    implementation(projects.feature.tokenDetailsApi)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.tradingview.lightweightcharts)
}
