plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("convention") {
            id = "indiana.convention"
            implementationClass = "IndianaConventionPlugin"
        }
    }
}
