package com.namansuthar.games.widget.tictactoe

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.namansuthar.games.core.data.repository.GameStateRepository
import com.namansuthar.games.engine.GameResult
import com.namansuthar.games.tictactoe.TicTacToeEngine
import com.namansuthar.games.tictactoe.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Glance widget for Tic-Tac-Toe game.
 */
class TicTacToeWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(200.dp, 200.dp),
            DpSize(250.dp, 250.dp),
            DpSize(300.dp, 300.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            TicTacToeWidgetContent(glanceId = id)
        }
    }
}

@Composable
fun TicTacToeWidgetContent(glanceId: GlanceId) {
    val context = LocalContext.current
    val widgetId = glanceId.toString()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(android.graphics.Color.parseColor("#1E293B")))
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tic-Tac-Toe",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(android.graphics.Color.WHITE)
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Game board - placeholder for now
            TicTacToeBoard(
                widgetId = widgetId
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Controls
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(
                    text = "New Game",
                    onClick = actionRunCallback<NewGameAction>(
                        actionParametersOf(WIDGET_ID_KEY to widgetId)
                    )
                )
            }
        }
    }
}

@Composable
private fun TicTacToeBoard(widgetId: String) {
    val cellSize = 60.dp

    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in 0..2) {
            Row {
                for (col in 0..2) {
                    val position = Position(row, col)
                    GameCell(
                        widgetId = widgetId,
                        position = position,
                        size = cellSize
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCell(
    widgetId: String,
    position: Position,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = GlanceModifier
            .size(size)
            .padding(2.dp)
            .background(ColorProvider(android.graphics.Color.parseColor("#334155")))
            .cornerRadius(4.dp)
            .clickable(
                actionRunCallback<PlaceMarkerAction>(
                    actionParametersOf(
                        WIDGET_ID_KEY to widgetId,
                        ROW_KEY to position.row,
                        COL_KEY to position.col
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Cell content will be rendered based on state
        // For now, just empty cells
    }
}

@Composable
private fun ControlButton(
    text: String,
    onClick: androidx.glance.action.Action
) {
    Box(
        modifier = GlanceModifier
            .height(32.dp)
            .padding(horizontal = 12.dp)
            .background(ColorProvider(android.graphics.Color.parseColor("#3B82F6")))
            .cornerRadius(8.dp)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(android.graphics.Color.WHITE)
            )
        )
    }
}

// Action parameter keys
private val WIDGET_ID_KEY = ActionParameters.Key<String>("widget_id")
private val ROW_KEY = ActionParameters.Key<Int>("row")
private val COL_KEY = ActionParameters.Key<Int>("col")

/**
 * Place a marker on the board.
 */
class PlaceMarkerAction : ActionCallback, KoinComponent {
    private val engine: TicTacToeEngine by inject()
    private val repository: GameStateRepository by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val widgetId = parameters[WIDGET_ID_KEY] ?: return
        val row = parameters[ROW_KEY] ?: return
        val col = parameters[COL_KEY] ?: return
        val position = Position(row, col)

        withContext(Dispatchers.IO) {
            val currentState = repository.getWidgetState(widgetId)?.let {
                engine.deserialize(it.stateData)
            } ?: engine.initialState()

            // Ignore if it's CPU's turn or cell is occupied
            if (currentState.isCpuTurn() || !currentState.isCellEmpty(position)) {
                return@withContext
            }

            // Place player's marker
            val playerResult = engine.processAction(
                currentState,
                TicTacToeAction.PlaceMarker(position)
            )

            if (playerResult is GameResult.Success) {
                var finalState = playerResult.newState

                // Save player's move
                repository.saveWidgetState(
                    widgetId = widgetId,
                    gameType = "tictactoe",
                    stateData = engine.serialize(finalState),
                    score = engine.getScore(finalState)
                )

                TicTacToeWidget().update(context, glanceId)

                // If game is not over and it's CPU's turn, make CPU move
                if (!finalState.isGameOver && finalState.isCpuTurn()) {
                    delay(300) // Small delay for better UX

                    val cpuMove = engine.getBestMove(finalState)
                    if (cpuMove != null) {
                        val cpuResult = engine.processAction(
                            finalState,
                            TicTacToeAction.PlaceMarker(cpuMove)
                        )

                        if (cpuResult is GameResult.Success) {
                            finalState = cpuResult.newState

                            repository.saveWidgetState(
                                widgetId = widgetId,
                                gameType = "tictactoe",
                                stateData = engine.serialize(finalState),
                                score = engine.getScore(finalState)
                            )

                            TicTacToeWidget().update(context, glanceId)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Start a new game.
 */
class NewGameAction : ActionCallback, KoinComponent {
    private val engine: TicTacToeEngine by inject()
    private val repository: GameStateRepository by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val widgetId = parameters[WIDGET_ID_KEY] ?: return

        withContext(Dispatchers.IO) {
            val currentState = repository.getWidgetState(widgetId)?.let {
                engine.deserialize(it.stateData)
            } ?: engine.initialState()

            // Create new game preserving stats
            val newState = engine.initialState().copy(
                xWins = currentState.xWins,
                oWins = currentState.oWins,
                draws = currentState.draws
            )

            repository.saveWidgetState(
                widgetId = widgetId,
                gameType = "tictactoe",
                stateData = engine.serialize(newState),
                score = 0
            )

            TicTacToeWidget().update(context, glanceId)
        }
    }
}
