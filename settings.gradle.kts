pluginManagement {
    pluginManagement {
        repositories {
            google {
                content {
                    includeGroupByRegex("com\\.android.*")
                    includeGroupByRegex("com\\.google.*")
                    includeGroupByRegex("androidx.*")
                }
            }
            maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") // Required for Compose Compiler plugin
            mavenCentral()
            gradlePluginPortal()
        }

        plugins {
            id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" // Kotlin plugin with Compose support
            id("org.jetbrains.kotlin.compose.compiler") version "1.5.11" // Compose compiler plugin
            id("com.android.application") version "8.4.0" // Replace with your actual AGP version
            id("org.jetbrains.kotlin.android") version "2.0.0"
        }
    }

    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            google()
            mavenCentral()
        }
    }

    rootProject.name = "Thinture Technologies Pvt Ltd"
    include(":app")

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Thinture Technologies Pvt Ltd"
include(":app")
 