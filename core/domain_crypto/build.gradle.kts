import java.util.Properties

plugins {
    alias(libs.plugins.fortera.android.library)
}

android {
    namespace = "com.yasashny.fortera.core.domaincrypto"
    buildFeatures { buildConfig = true }
    defaultConfig {
        val props = Properties()
        try {
            props.load(rootProject.file("local.properties").inputStream())
        } catch (e: Exception) {
            // local.properties may not exist in CI
        }
        buildConfigField("String", "INFURA_PROJECT_ID", "\"${props["INFURA_PROJECT_ID"] ?: ""}\"")
        buildConfigField("String", "COINSTATS_API_KEY", "\"${props["COINSTATS_API_KEY"] ?: ""}\"")
    }
}

dependencies {
    api(projects.core.common)
    api(projects.core.network)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bouncycastle)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.koin.androidx.compose)
}
