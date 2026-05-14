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
    implementation(projects.feature.tokenDetailsApi)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.zxing.core)
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.guava)
}
