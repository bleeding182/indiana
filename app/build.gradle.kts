@file:OptIn(KspExperimental::class)

import com.google.devtools.ksp.KspExperimental

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.performance)
    id("com.davidmedenjak.fontsubsetting") version "0.0.2"
}

android {
    namespace = "com.davidmedenjak.indiana"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.davidmedenjak.indiana"
        minSdk = 24
        targetSdk = 36
        versionCode = 17
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

fontSubsetting {
    fonts {
        create("materialSymbols") {
            fontFile.set(file("symbolfonts/MaterialSymbolsOutlined.ttf"))
            codepointsFile.set(file("symbolfonts/MaterialSymbolsOutlined.codepoints"))
            packageName.set("com.davidmedenjak.fontsubsetting")
            className.set("MaterialSymbols")
            // resourceName and fontFileName will default based on font file name
            // but we can override them if needed:
            resourceName.set("symbols")
            fontFileName.set("symbols.ttf")

            // Configure variable font axes
            axes {
                // Keep fill axis but limit to 0..1 range
                axis("FILL").range(0f, 1f, 0f)

                // Limit weight to 400-700 range (normal to bold)
                axis("wght").range(400f, 700f, 400f)

                // Remove grade axis completely
                axis("GRAD").remove()

                // Keep optical size but limit to 24-48 range
                axis("opsz").range(24f, 48f, 48f)
            }
        }
    }
}

ksp {
    useKsp2 = false
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(project(":api"))
    implementation(project(":theme"))

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose.hilt)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.playservices.auth)

    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)

    implementation(libs.dagger)
    implementation(libs.dagger.hilt)
    implementation(libs.androidx.foundation.android)
    ksp(libs.dagger.compiler)
    ksp(libs.dagger.compiler.hilt)
    ksp(libs.dagger.compiler.hilt.androidx)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.performance)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}