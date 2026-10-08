package com.namansuthar.games

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.namansuthar.games.feature.home.HomeScreen
import com.namansuthar.games.game2048.ui.Game2048Screen
import com.namansuthar.games.snake.ui.SnakeScreen

/**
 * Main app composable with navigation.
 */
@Composable
fun GameApp(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onGameClick = { gameId ->
                    navController.navigate("game/$gameId")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        composable(
            route = "game/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable

            when (gameId) {
                "2048" -> {
                    Game2048Screen(
                        onBack = { navController.popBackStack() }
                    )
                }
                "snake" -> {
                    SnakeScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                else -> {
                    // Placeholder for other games
                    GamePlaceholderScreen(
                        gameId = gameId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        composable("settings") {
            // Placeholder for settings screen
            SettingsPlaceholderScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun GamePlaceholderScreen(
    gameId: String,
    onBack: () -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text(gameId.uppercase()) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "$gameId game coming soon...",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun SettingsPlaceholderScreen(onBack: () -> Unit) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("Settings") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "Settings coming soon...",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
        }
    }
}
