plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.main"
}

dependencies {
    implementation(projects.core.domainWallet)
    implementation(projects.core.domainCrypto)
    implementation(projects.feature.mainApi)
    implementation(projects.feature.settingsApi)
    implementation(projects.feature.startupApi)
    implementation(projects.feature.manageTokensApi)
    implementation(projects.feature.tokenDetailsApi)
    implementation(projects.feature.receiveApi)
    implementation(projects.feature.walletSelector)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.compose.material.icons.extended)
}
