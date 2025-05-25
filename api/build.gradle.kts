import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.ksp)
}

openApiGenerate {
    generatorName = "kotlin"
    inputSpec = project.file("bitrise.json").path
    outputDir = project.layout.buildDirectory.asFile.get().path
    modelPackage = "com.davidmedenjak.indiana.model"
    apiPackage = "com.davidmedenjak.indiana.api"
    modelFilesConstrainedTo.add("")
    apiFilesConstrainedTo.add("")
    supportingFilesConstrainedTo.add("CollectionFormats.kt")
    generateModelDocumentation = false
    generateModelTests = false
//    cleanupOutput = true
    additionalProperties.putAll(
        mapOf(
            "library" to "jvm-retrofit2",
            "serializationLibrary" to "moshi",
            "moshiCodeGen" to "true",
            "useCoroutines" to "true",
            "omitGradleWrapper" to "true",
            "sourceFolder" to "bitrise-swagger",
            "useSettingsGradle" to "false",
            "useResponseAsReturnType" to "false",
        )
    )

}

android {
    namespace = "com.davidmedenjak.api"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    sourceSets["main"].kotlin.srcDirs(project.layout.buildDirectory.dir("bitrise-swagger"))
    tasks.preBuild {
        dependsOn(tasks.withType<GenerateTask>())
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)
    implementation(libs.retrofit)
}