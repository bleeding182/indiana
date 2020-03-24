plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:3.6.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.71")
    implementation(gradleApi())
}