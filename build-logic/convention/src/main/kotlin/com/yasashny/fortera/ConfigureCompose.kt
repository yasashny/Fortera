package com.yasashny.fortera

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.configureCompose(
    extension: ApplicationExtension,
) {
    extension.apply {
        buildFeatures {
            compose = true
        }
        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }
    }

    configureComposeDependencies()
}

internal fun Project.configureCompose(
    extension: LibraryExtension,
) {
    extension.apply {
        buildFeatures {
            compose = true
        }
        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }
    }

    configureComposeDependencies()
}

private fun Project.configureComposeDependencies() {
    dependencies {
        implementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(platform(libs.androidx.compose.bom))
        debugImplementation(libs.androidx.compose.ui.tooling)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.material.icons)
    }

    composeCompiler {
        metricsDestination = layout.buildDirectory.dir("compose_compiler")
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
    }
}

fun Project.composeCompiler(configure: Action<ComposeCompilerGradlePluginExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure(
        "composeCompiler", configure
    )
