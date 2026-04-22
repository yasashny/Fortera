plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.receive"
}

dependencies {
    implementation(projects.core.domainWallet)
    implementation(projects.core.domainCrypto)
    implementation(projects.core.walletBalances)
    implementation(projects.feature.receiveApi)
    implementation(projects.feature.mainApi)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.zxing.core)
}
