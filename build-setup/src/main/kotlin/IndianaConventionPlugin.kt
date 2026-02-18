import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class IndianaConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withId("com.android.application") { configureAndroid(target) }
        target.plugins.withId("com.android.library") { configureAndroid(target) }
        target.plugins.withId("org.jetbrains.kotlin.android") { configureKotlinAndroid(target) }
        target.plugins.withId("org.jetbrains.kotlin.jvm") { configureKotlinJvm(target) }
    }

    private fun configureAndroid(target: Project) {
        val android = target.extensions.getByType(CommonExtension::class.java)
        android.compileSdk = 36
        android.defaultConfig.minSdk = 24
        android.compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        android.compileOptions.targetCompatibility = JavaVersion.VERSION_17
    }

    private fun configureKotlinAndroid(target: Project) {
        target.extensions.configure(KotlinAndroidProjectExtension::class.java) {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }

    private fun configureKotlinJvm(target: Project) {
        target.extensions.configure(JavaPluginExtension::class.java) {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        target.extensions.configure(KotlinJvmProjectExtension::class.java) {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }
}
