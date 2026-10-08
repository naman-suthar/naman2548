package com.namansuthar.games.tictactoe.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.namansuthar.games.tictactoe.model.*
import org.koin.androidx.compose.koinViewModel
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tic-Tac-Toe game screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeScreen(
    viewModel: TicTacToeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tic-Tac-Toe") },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats
            StatsSection(state)

            Spacer(modifier = Modifier.height(24.dp))

            // Game board
            TicTacToeBoard(
                state = state,
                onCellClick = { position -> viewModel.placeMarker(position) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Controls
            ControlsSection(
                state = state,
                onNewGame = { viewModel.newGame() },
                onUndo = { viewModel.undo() }
            )
        }
    }

    // Game over dialog
    if (state.isGameOver) {
        GameOverDialog(
            state = state,
            onDismiss = { viewModel.newGame() }
        )
    }

    // Settings dialog
    if (showSettings) {
        SettingsDialog(
            state = state,
            onDismiss = { showSettings = false },
            onGameModeChange = { viewModel.setGameMode(it) },
            onDifficultyChange = { viewModel.setDifficulty(it) }
        )
    }
}

@Composable
private fun StatsSection(state: TicTacToeState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCard(
            label = "X Wins",
            value = state.xWins,
            color = MaterialTheme.colorScheme.primary
        )
        StatCard(
            label = "Draws",
            value = state.draws,
            color = MaterialTheme.colorScheme.secondary
        )
        StatCard(
            label = "O Wins",
            value = state.oWins,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun StatCard(label: String, value: Int, color: Color) {
    Card(
        modifier = Modifier.size(100.dp, 80.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TicTacToeBoard(
    state: TicTacToeState,
    onCellClick: (Position) -> Unit
) {
    val boardSize = 300.dp
    val cellSize = boardSize / 3

    Box(
        modifier = Modifier
            .size(boardSize)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            for (row in 0..2) {
                Row {
                    for (col in 0..2) {
                        val position = Position(row, col)
                        val player = state.getCell(position)
                        val isWinningCell = state.winningLine?.contains(position) == true

                        GameCell(
                            player = player,
                            isWinningCell = isWinningCell,
                            onClick = { onCellClick(position) },
                            modifier = Modifier.size(cellSize)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCell(
    player: Player,
    isWinningCell: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isWinningCell -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val scale by animateFloatAsState(
        targetValue = if (player != Player.NONE) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cell_scale"
    )

    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = player == Player.NONE) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (player) {
            Player.X -> {
                val color = MaterialTheme.colorScheme.primary
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                ) {
                    drawX(color)
                }
            }
            Player.O -> {
                val color = MaterialTheme.colorScheme.tertiary
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                ) {
                    drawO(color)
                }
            }
            Player.NONE -> {}
        }
    }
}

@Composable
private fun Canvas(
    modifier: Modifier = Modifier,
    onDraw: DrawScope.() -> Unit
) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}

private fun DrawScope.drawX(color: Color) {
    val strokeWidth = 8.dp.toPx()

    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    drawLine(
        color = color,
        start = Offset(size.width, 0f),
        end = Offset(0f, size.height),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawO(color: Color) {
    val strokeWidth = 8.dp.toPx()

    drawCircle(
        color = color,
        radius = size.minDimension / 2,
        style = Stroke(width = strokeWidth)
    )
}

@Composable
private fun ControlsSection(
    state: TicTacToeState,
    onNewGame: () -> Unit,
    onUndo: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onNewGame,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("New Game")
        }

        OutlinedButton(
            onClick = onUndo,
            enabled = state.moveHistory.isNotEmpty() && !state.isGameOver,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Undo, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Undo")
        }
    }
}

@Composable
private fun GameOverDialog(
    state: TicTacToeState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = when {
                    state.winner != null -> Icons.Default.EmojiEvents
                    else -> Icons.Default.Handshake
                },
                contentDescription = null,
                tint = when {
                    state.winner == Player.X -> MaterialTheme.colorScheme.primary
                    state.winner == Player.O -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.secondary
                }
            )
        },
        title = {
            Text(
                text = when {
                    state.winner == Player.X -> "Player X Wins!"
                    state.winner == Player.O -> "Player O Wins!"
                    else -> "It's a Draw!"
                }
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Overall Stats:")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "X: ${state.xWins}  |  Draws: ${state.draws}  |  O: ${state.oWins}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("New Game")
            }
        }
    )
}

@Composable
private fun SettingsDialog(
    state: TicTacToeState,
    onDismiss: () -> Unit,
    onGameModeChange: (GameMode) -> Unit,
    onDifficultyChange: (Difficulty) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
        title = { Text("Game Settings") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Game Mode
                Text(
                    "Game Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                GameMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onGameModeChange(mode) }
                            .background(
                                if (state.gameMode == mode) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    Color.Transparent
                                }
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.gameMode == mode,
                            onClick = { onGameModeChange(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (mode) {
                                GameMode.PLAYER_VS_PLAYER -> "Player vs Player"
                                GameMode.PLAYER_VS_CPU -> "Player vs CPU"
                                GameMode.CPU_VS_CPU -> "CPU vs CPU"
                            }
                        )
                    }
                }

                Divider()

                // Difficulty
                Text(
                    "CPU Difficulty",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Difficulty.entries.forEach { difficulty ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDifficultyChange(difficulty) }
                            .background(
                                if (state.difficulty == difficulty) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    Color.Transparent
                                }
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.difficulty == difficulty,
                            onClick = { onDifficultyChange(difficulty) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
