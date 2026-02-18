plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.ksp)
    id("indiana.convention")
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

sourceSets["main"].kotlin.srcDir(layout.buildDirectory.dir("bitrise-swagger"))

tasks.configureEach {
    if (name == "kspKotlin" || this is org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>) {
        dependsOn("openApiGenerate")
    }
}

dependencies {
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)
    implementation(libs.retrofit)
}
