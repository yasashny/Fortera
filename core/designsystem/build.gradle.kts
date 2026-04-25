plugins {
    alias(libs.plugins.fortera.android.library)
    alias(libs.plugins.fortera.android.library.compose)
}

android {
    namespace = "com.yasashny.fortera.core.designsystem"
}

dependencies {
    api(projects.core.common)
}
