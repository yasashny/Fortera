plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.settings"
    buildFeatures { buildConfig = true }
}

dependencies {
    implementation(projects.feature.settingsApi)
    implementation(projects.core.network)
    implementation(projects.core.domainCrypto)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)
}
