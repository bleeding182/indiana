plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:3.5.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
    implementation(gradleApi())
}