pluginManagement {
    repositories {
        google()
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

rootProject.name = "BoardGamesWidgetApp"

// App module
include(":app")

// Core modules
include(":core:ui")
include(":core:domain")
include(":core:data")
include(":core:common")

// Game modules
include(":game:engine")
include(":game:game-2048")
include(":game:game-snake")
include(":game:game-tictactoe")

// Widget modules
include(":widget:framework")
include(":widget:widget-2048")
include(":widget:widget-snake")
include(":widget:widget-tictactoe")

// Feature modules
include(":feature:home")
include(":feature:settings")

// Design system
include(":design-system")
