pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://androidx.dev/snapshots/builds/13508953/artifacts/repository")
        }

        // Load credentials from local.properties
        val localProperties = java.util.Properties()
        val localPropertiesFile = file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }
        maven("https://maven.pkg.github.com/bleeding182/icon-font-subset-plugin") {
            credentials {
                username = localProperties.getProperty("gpr.user") ?: System.getenv("GPR_USER")
                password = localProperties.getProperty("gpr.token") ?: System.getenv("GPR_TOKEN")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://androidx.dev/snapshots/builds/13508953/artifacts/repository")
        }
    }
}

rootProject.name = "Indiana"
include(":app")
include(":api")
include(":theme")
