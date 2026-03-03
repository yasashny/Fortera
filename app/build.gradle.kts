plugins {
    alias(libs.plugins.fortera.android.application)
    alias(libs.plugins.fortera.android.application.compose)
}

android {
    namespace = "com.yasashny.fortera"

    defaultConfig {
        applicationId = "com.yasashny.fortera"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // Core modules
    implementation(projects.core.domainCrypto)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.domainWallet)
    implementation(projects.core.designsystem)
    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.core.navigation)
    implementation(projects.core.mvi)

    // Feature modules
    implementation(projects.feature.startupApi)
    implementation(projects.feature.startup)
    implementation(projects.feature.importWalletApi)
    implementation(projects.feature.importWallet)
    implementation(projects.feature.createWalletApi)
    implementation(projects.feature.createWallet)
    implementation(projects.feature.mainApi)
    implementation(projects.feature.main)
    implementation(projects.feature.walletSelectorApi)
    implementation(projects.feature.walletSelector)
    implementation(projects.feature.settingsApi)
    implementation(projects.feature.settings)
    implementation(projects.feature.manageTokensApi)
    implementation(projects.feature.manageTokens)
    implementation(projects.feature.tokenDetailsApi)
    implementation(projects.feature.tokenDetails)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // App lock
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Compose
    implementation(libs.androidx.activity.compose)

    // DI
    implementation(libs.koin.androidx.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
