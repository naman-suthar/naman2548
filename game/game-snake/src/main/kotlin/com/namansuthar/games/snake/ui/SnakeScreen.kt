package com.namansuthar.games.snake.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.namansuthar.games.snake.model.Direction
import com.namansuthar.games.snake.model.Position

/**
 * Main screen for the Snake game.
 * Features auto-play with pause/resume and speed controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SnakeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Snake") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onNewGame() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "New Game")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Score and stats
            StatsRow(
                score = state.score,
                speed = state.speed,
                length = state.snake.size
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // Game grid
            SnakeGrid(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // Controls
            GameControls(
                isPaused = state.isPaused,
                isGameOver = state.isGameOver,
                speed = state.speed,
                onPauseToggle = { viewModel.onPauseToggle() },
                onDirectionChange = { viewModel.onDirectionChange(it) },
                onSpeedIncrease = { viewModel.onSpeedIncrease() },
                onSpeedDecrease = { viewModel.onSpeedDecrease() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Game over dialog
    if (state.isGameOver) {
        GameOverDialog(
            score = state.score,
            length = state.snake.size,
            onNewGame = { viewModel.onNewGame() }
        )
    }
}

@Composable
private fun StatsRow(
    score: Int,
    speed: Int,
    length: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCard(label = "SCORE", value = score.toString())
        StatCard(label = "SPEED", value = "${speed}x")
        StatCard(label = "LENGTH", value = length.toString())
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(100.dp).height(70.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun SnakeGrid(
    state: com.namansuthar.games.snake.model.SnakeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (y in 0 until state.gridSize) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (x in 0 until state.gridSize) {
                        val position = Position(x, y)
                        GridCell(
                            isSnake = state.isSnakeAt(position),
                            isHead = state.head == position,
                            isFood = state.isFoodAt(position),
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridCell(
    isSnake: Boolean,
    isHead: Boolean,
    isFood: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isFood -> Color(0xFFE74C3C) // Red food
        isHead -> Color(0xFF2ECC71) // Green head
        isSnake -> Color(0xFF27AE60) // Darker green body
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .clip(if (isFood) CircleShape else RoundedCornerShape(2.dp))
            .background(backgroundColor)
    )
}

@Composable
private fun GameControls(
    isPaused: Boolean,
    isGameOver: Boolean,
    speed: Int,
    onPauseToggle: () -> Unit,
    onDirectionChange: (Direction) -> Unit,
    onSpeedIncrease: () -> Unit,
    onSpeedDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pause/Resume button
        Button(
            onClick = onPauseToggle,
            enabled = !isGameOver,
            modifier = Modifier.width(200.dp)
        ) {
            Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isPaused) "RESUME" else "PAUSE")
        }

        // Directional controls
        DirectionalPad(
            onDirectionChange = onDirectionChange,
            enabled = !isGameOver && !isPaused
        )

        // Speed controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSpeedDecrease,
                enabled = speed > 1 && !isGameOver
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease speed")
            }

            Text(
                text = "Speed: ${speed}x",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onSpeedIncrease,
                enabled = speed < com.namansuthar.games.snake.model.SnakeState.MAX_SPEED && !isGameOver
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase speed")
            }
        }
    }
}

@Composable
private fun DirectionalPad(
    onDirectionChange: (Direction) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up button
        DirectionButton(
            icon = Icons.Default.KeyboardArrowUp,
            onClick = { onDirectionChange(Direction.UP) },
            enabled = enabled
        )

        Row {
            // Left button
            DirectionButton(
                icon = Icons.Default.KeyboardArrowLeft,
                onClick = { onDirectionChange(Direction.LEFT) },
                enabled = enabled
            )

            Spacer(modifier = Modifier.width(60.dp))

            // Right button
            DirectionButton(
                icon = Icons.Default.KeyboardArrowRight,
                onClick = { onDirectionChange(Direction.RIGHT) },
                enabled = enabled
            )
        }

        // Down button
        DirectionButton(
            icon = Icons.Default.KeyboardArrowDown,
            onClick = { onDirectionChange(Direction.DOWN) },
            enabled = enabled
        )
    }
}

@Composable
private fun DirectionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(56.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun GameOverDialog(
    score: Int,
    length: Int,
    onNewGame: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "Game Over!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Final Score: $score")
                Text("Snake Length: $length")
            }
        },
        confirmButton = {
            Button(onClick = onNewGame) {
                Text("NEW GAME")
            }
        }
    )
}
