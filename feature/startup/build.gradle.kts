plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.startup"
}

dependencies {
    implementation(projects.core.domainWallet)

    implementation(libs.androidx.activity.compose)
    // Own api
    implementation(projects.feature.startupApi)
    // Cross-feature navigation
    implementation(projects.feature.createWalletApi)
    implementation(projects.feature.importWalletApi)
    implementation(projects.feature.mainApi)
}
