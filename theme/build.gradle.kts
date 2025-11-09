import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.font.subsetting)
}

android {
    namespace = "com.davidmedenjak.indiana.theme"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(JavaVersion.VERSION_17.toString())
        }
    }
    buildFeatures {
        compose = true
    }
}

fontSubsetting {
    fonts {
        create("materialSymbols") {
            fontFile.set(file("symbolfonts/MaterialSymbolsOutlined.ttf"))
            codepointsFile.set(file("symbolfonts/MaterialSymbolsOutlined.codepoints"))
            className.set("com.davidmedenjak.fontsubsetting.MaterialSymbols")
            // resourceName and fontFileName will default based on font file name
            // but we can override them if needed:
            resourceName.set("symbols")

            // Configure variable font axes
            axes {
                // Keep fill axis but limit to 0..1 range
                axis("FILL").range(0f, 1f, 0f)

                // Limit weight to 400-700 range (normal to bold)
                axis("wght").range(400f, 700f, 400f)

//                // Remove grade axis completely
//                axis("GRAD").remove()

                // Keep optical size but limit to 24-48 range
                axis("opsz").range(24f, 48f, 48f)
            }
            stripGlyphNames = true
            stripHinting = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    debugImplementation(libs.ui.tooling)
}