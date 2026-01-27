package com.yasashny.fortera

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureKotlinAndroid(
    extension: ApplicationExtension,
) {
    extension.apply {
        compileSdk = 36

        defaultConfig {
            minSdk = 29
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            isCoreLibraryDesugaringEnabled = true
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    dependencies {
        coreLibraryDesugaring(libs.desugar.jdk.libs)
        implementation(libs.androidx.core.ktx)
    }
}

internal fun Project.configureKotlinAndroid(
    extension: LibraryExtension,
) {
    extension.apply {
        compileSdk = 36

        defaultConfig {
            minSdk = 29
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            isCoreLibraryDesugaringEnabled = true
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    dependencies {
        coreLibraryDesugaring(libs.desugar.jdk.libs)
        implementation(libs.androidx.core.ktx)
    }
}
