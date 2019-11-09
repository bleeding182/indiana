package at.allaboutapps.build

import com.android.build.gradle.*
import org.gradle.api.JavaVersion
import org.gradle.api.JavaVersion.VERSION_1_8
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

class BuildPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.all { checkPlugin(this, project) }
    }

    private fun checkPlugin(plugin: Plugin<*>, project: Project) {
        when (plugin) {
            is JavaPlugin,
            is JavaLibraryPlugin -> {
                val convention = project.convention.getPlugin<JavaPluginConvention>()
                convention.apply {
                    sourceCompatibility = VERSION_1_8
                    targetCompatibility = VERSION_1_8
                }
                project.afterEvaluate {
                    verifyJava8Targeting(convention.sourceCompatibility)
                }
            }
            is LibraryPlugin -> {
                project.extensions.getByType<LibraryExtension>().apply {
                    configureAndroidCommonOptions(project)
                }
            }
            is AppPlugin -> {
                project.extensions.getByType<AppExtension>().apply {
                    configureAndroidCommonOptions(project)
                    configureAndroidApplicationOptions(project)
                }
            }
            is KotlinBasePluginWrapper -> {
                project.tasks.withType<KotlinJvmCompile> {
                    kotlinOptions {
                        jvmTarget = VERSION_1_8.toString()
                    }
                }
            }
        }
    }

    private fun TestedExtension.configureAndroidCommonOptions(project: Project) {
        compileOptions.apply {
            sourceCompatibility = VERSION_1_8
            targetCompatibility = VERSION_1_8
        }

        compileSdkVersion(COMPILE_SDK_VERSION)
        defaultConfig.targetSdkVersion(TARGET_SDK_VERSION)

        testOptions.unitTests.isReturnDefaultValues = true

        defaultConfig.minSdkVersion(DEFAULT_MIN_SDK_VERSION)

        project.afterEvaluate {
            val minSdkVersion = defaultConfig.minSdkVersion.apiLevel
            check(minSdkVersion >= DEFAULT_MIN_SDK_VERSION) {
                "minSdkVersion $minSdkVersion bigger than the default of $DEFAULT_MIN_SDK_VERSION"
            }
            verifyJava8Targeting(compileOptions.sourceCompatibility)
        }
    }

    private fun verifyJava8Targeting(javaVersion: JavaVersion) {
        check(javaVersion == VERSION_1_8) {
            "You're targeting Java $javaVersion. Please remove `sourceCompatibility = $javaVersion` from build.gradle"
        }
    }

    private fun AppExtension.configureAndroidApplicationOptions(project: Project) {
        defaultConfig.apply {
            versionCode = 1
            versionName = "1.0"
        }
    }

    companion object {
        const val COMPILE_SDK_VERSION = 29
        const val TARGET_SDK_VERSION = 29
        const val DEFAULT_MIN_SDK_VERSION = 16
    }
}
