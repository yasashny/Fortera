plugins {
    alias(libs.plugins.fortera.android.feature)
}

android {
    namespace = "com.yasashny.fortera.feature.settings"
}

dependencies {
    implementation(projects.feature.settingsApi)
}
