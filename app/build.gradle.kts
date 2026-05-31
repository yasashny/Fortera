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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DISCLAIMER",
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*.kotlin_module",
                "jni/**",
                "ethereum/ckzg4844/lib/**",
                "trusted_setup.txt",
                "org/bouncycastle/pqc/**",
                "org/bouncycastle/x509/*.properties",
                "org/bouncycastle/i18n/**",
                "org.bitcoin.*.checkpoints.txt",
                "org/bitcoinj/crypto/cacerts",
                "**/*.proto",
            )
        }
    }
}

dependencies {
    implementation(projects.core.domainCrypto)
    implementation(projects.core.dataCrypto)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.domainWallet)
    implementation(projects.core.walletBalances)
    implementation(projects.core.designsystem)
    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.core.navigation)
    implementation(projects.core.mvi)

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
    implementation(projects.feature.receiveApi)
    implementation(projects.feature.receive)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.activity.compose)

    implementation(libs.koin.androidx.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
