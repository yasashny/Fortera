import com.android.build.api.dsl.LibraryExtension
import com.yasashny.fortera.configureKotlinAndroid
import com.yasashny.fortera.libs
import com.yasashny.fortera.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.android.library.get().pluginId)
            }

            pluginManager.withPlugin(libs.plugins.kotlin.android.get().pluginId) {
                extensions.configure<KotlinAndroidProjectExtension> {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_17)
                    }
                }
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.consumerProguardFiles("consumer-rules.pro")
            }

            dependencies {
                testImplementation(libs.junit)
                testImplementation(libs.kotlinx.coroutines.test)
                testImplementation(libs.mockk)
            }
        }
    }
}
