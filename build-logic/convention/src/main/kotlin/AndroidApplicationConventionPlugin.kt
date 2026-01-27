import com.android.build.api.dsl.ApplicationExtension
import com.yasashny.fortera.configureKotlinAndroid
import com.yasashny.fortera.implementation
import com.yasashny.fortera.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.android.application.get().pluginId)
            }

            pluginManager.withPlugin(libs.plugins.kotlin.android.get().pluginId) {
                extensions.configure<KotlinAndroidProjectExtension> {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_17)
                    }
                }
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 36
            }

            dependencies {
                implementation(libs.androidx.lifecycle.runtime.ktx)
            }
        }
    }
}
