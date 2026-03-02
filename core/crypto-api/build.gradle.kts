import java.util.Properties

plugins {
    alias(libs.plugins.fortera.android.library)
}

android {
    namespace = "com.yasashny.fortera.core.cryptoapi"
    buildFeatures { buildConfig = true }
    defaultConfig {
        val props = Properties()
        try {
            props.load(rootProject.file("local.properties").inputStream())
        } catch (e: Exception) {
            // local.properties may not exist in CI
        }
        buildConfigField("String", "INFURA_PROJECT_ID", "\"${props["INFURA_PROJECT_ID"] ?: ""}\"")
    }
}

dependencies {
    api(projects.core.common)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.bouncycastle)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.koin.androidx.compose)
}
