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
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "voice-journal"

// ODPM layering:
//  :domain — the ontology expressed as pure Kotlin (concepts, relationships, behaviors, states, rules)
//  :app    — the Android expression of that ontology (UI, persistence, audio hardware)
include(":domain")
include(":app")
