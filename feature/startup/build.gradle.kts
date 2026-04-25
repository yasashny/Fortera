plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.startup"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(projects.feature.startupApi)
    implementation(projects.feature.createWalletApi)
    implementation(projects.feature.importWalletApi)
    implementation(projects.feature.mainApi)
}
