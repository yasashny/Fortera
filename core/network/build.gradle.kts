plugins {
    alias(libs.plugins.fortera.android.library)
}

android {
    namespace = "com.yasashny.fortera.core.network"
}

dependencies {
    api(projects.core.common)
    api(libs.ktor.client.android)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.koin.androidx.compose)
}
