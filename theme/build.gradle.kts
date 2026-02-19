plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("indiana.convention")
}

ksp {
    arg("apidoc.outputDir", layout.buildDirectory.dir("generated/apidoc").get().asFile.absolutePath)
    arg("apidoc.moduleRoot", projectDir.absolutePath)
}

android {
    namespace = "com.davidmedenjak.indiana.theme"

    buildFeatures {
        compose = true
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
    implementation(libs.androidx.material.icons)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(project(":apidoc-annotations"))
    ksp(project(":apidoc"))
    lintChecks(project(":lint"))
    debugImplementation(libs.androidx.ui.tooling)
}