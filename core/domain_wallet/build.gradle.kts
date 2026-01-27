plugins {
    alias(libs.plugins.fortera.android.library)
    alias(libs.plugins.fortera.android.room)
}

android {
    namespace = "com.yasashny.fortera.core.domain.wallet"
}

dependencies {
    api(projects.core.common)
    implementation(projects.core.database)

    implementation(libs.kotlin.bip39)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.koin.androidx.compose)
}
