import com.android.build.api.dsl.LibraryExtension
import com.yasashny.fortera.configureCompose
import com.yasashny.fortera.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.kotlin.compose.get().pluginId)
            }

            extensions.configure<LibraryExtension> {
                configureCompose(this)
            }
        }
    }
}
