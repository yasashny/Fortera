import com.yasashny.fortera.implementation
import com.yasashny.fortera.libs
import com.yasashny.fortera.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("fortera.android.library")
                apply("fortera.android.library.compose")
            }

            dependencies {
                implementation(project(":core:designsystem"))
                implementation(project(":core:ui"))
                implementation(project(":core:common"))

                implementation(project(":core:mvi"))
                implementation(project(":core:navigation"))

                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.koin.androidx.compose)
            }
        }
    }
}
