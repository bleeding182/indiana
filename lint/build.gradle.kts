plugins {
    alias(libs.plugins.kotlin.jvm)
    id("indiana.convention")
}

dependencies {
    compileOnly(libs.lint.api)
    compileOnly(libs.lint.checks)
    testImplementation(libs.lint.api)
    testImplementation(libs.lint.tests)
    testImplementation(libs.junit)
}
