pluginManagement {
    includeBuild("build-logic")
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

rootProject.name = "Fortera"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

// Core modules
include(":core:crypto-api")
include(":core:database")
include(":core:domain_wallet")
include(":core:designsystem")
include(":core:common")
include(":core:ui")
include(":core:navigation")
include(":core:mvi")

// Feature modules
include(":feature:startup-api")
include(":feature:startup")

include(":feature:create-wallet-api")
include(":feature:create-wallet")

include(":feature:import-wallet-api")
include(":feature:import-wallet")

include(":feature:main-api")
include(":feature:main")

include(":feature:wallet-selector-api")
include(":feature:wallet-selector")

include(":feature:settings-api")
include(":feature:settings")

include(":feature:manage-tokens-api")
include(":feature:manage-tokens")
