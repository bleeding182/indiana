plugins {
    alias(libs.plugins.kotlin.jvm)
    id("indiana.convention")
}

dependencies {
    implementation(libs.ksp.api)
}
