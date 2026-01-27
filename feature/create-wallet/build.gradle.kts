plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.createwallet"
}

dependencies {
    implementation(projects.core.domainWallet)
    implementation(projects.feature.createWalletApi)
    implementation(projects.feature.mainApi)
}
