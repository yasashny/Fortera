plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.managetokens"
}

dependencies {
    implementation(projects.core.domainCrypto)
    implementation(projects.feature.manageTokensApi)
    implementation(libs.androidx.datastore.preferences)
}
