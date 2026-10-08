package com.namansuthar.games.game2048.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.namansuthar.games.game2048.model.Direction
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Main screen for the 2048 game.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Game2048Screen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: Game2048ViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("2048") },
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
            // Score display
            ScoreBoard(
                score = state.score,
                moves = state.moves,
                canUndo = state.previousGrids.isNotEmpty(),
                onUndo = { viewModel.onUndo() }
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // Game grid
            GameGrid(
                grid = state.grid,
                onSwipe = { direction -> viewModel.onMove(direction) },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Game status
            if (state.hasWon && !state.isGameOver) {
                Text(
                    text = "🎉 You reached 2048!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Win dialog
    if (uiState.showWinDialog) {
        WinDialog(
            score = state.score,
            onKeepPlaying = { viewModel.onKeepPlaying() },
            onNewGame = { viewModel.onNewGame() }
        )
    }

    // Game over dialog
    if (uiState.showGameOverDialog) {
        GameOverDialog(
            score = state.score,
            onNewGame = { viewModel.onNewGame() },
            onDismiss = { viewModel.onGameOverDialogDismiss() }
        )
    }
}

@Composable
private fun ScoreBoard(
    score: Int,
    moves: Int,
    canUndo: Boolean,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score
        ScoreCard(label = "SCORE", value = score)

        // Moves
        ScoreCard(label = "MOVES", value = moves)

        // Undo button
        Button(
            onClick = onUndo,
            enabled = canUndo,
            modifier = Modifier.height(60.dp)
        ) {
            Text("UNDO")
        }
    }
}

@Composable
private fun ScoreCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(100.dp).height(60.dp),
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
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun GameGrid(
    grid: List<List<Int>>,
    onSwipe: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableStateOf(Pair(0f, 0f)) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val (dx, dy) = dragOffset

                        if (abs(dx) > abs(dy)) {
                            // Horizontal swipe
                            if (abs(dx) > 50) {
                                onSwipe(if (dx > 0) Direction.RIGHT else Direction.LEFT)
                            }
                        } else {
                            // Vertical swipe
                            if (abs(dy) > 50) {
                                onSwipe(if (dy > 0) Direction.DOWN else Direction.UP)
                            }
                        }

                        dragOffset = Pair(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset = Pair(
                            dragOffset.first + dragAmount.x,
                            dragOffset.second + dragAmount.y
                        )
                    }
                )
            }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            grid.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { value ->
                        GameTile(
                            value = value,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameTile(
    value: Int,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getTileColor(value)
    val textColor = if (value <= 4) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.White
    }

    // Animate tile appearance
    val scale by animateFloatAsState(
        targetValue = if (value > 0) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tile_scale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (value > 0) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = when {
                        value < 100 -> 32.sp
                        value < 1000 -> 28.sp
                        else -> 24.sp
                    }
                ),
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun WinDialog(
    score: Int,
    onKeepPlaying: () -> Unit,
    onNewGame: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onKeepPlaying,
        title = {
            Text(
                text = "🎉 You Win!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("You reached 2048 with a score of $score!\n\nKeep playing to reach higher tiles!")
        },
        confirmButton = {
            TextButton(onClick = onKeepPlaying) {
                Text("KEEP PLAYING")
            }
        },
        dismissButton = {
            TextButton(onClick = onNewGame) {
                Text("NEW GAME")
            }
        }
    )
}

@Composable
private fun GameOverDialog(
    score: Int,
    onNewGame: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Game Over",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Final Score: $score\n\nNo more valid moves!")
        },
        confirmButton = {
            Button(onClick = onNewGame) {
                Text("NEW GAME")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE")
            }
        }
    )
}

/**
 * Get the background color for a tile based on its value.
 */
@Composable
private fun getTileColor(value: Int): Color {
    return when (value) {
        0 -> MaterialTheme.colorScheme.surface
        2 -> Color(0xFFEEE4DA)
        4 -> Color(0xFFEDE0C8)
        8 -> Color(0xFFF2B179)
        16 -> Color(0xFFF59563)
        32 -> Color(0xFFF67C5F)
        64 -> Color(0xFFF65E3B)
        128 -> Color(0xFFEDCF72)
        256 -> Color(0xFFEDCC61)
        512 -> Color(0xFFEDC850)
        1024 -> Color(0xFFEDC53F)
        2048 -> Color(0xFFEDC22E)
        else -> Color(0xFF3C3A32)
    }
}
