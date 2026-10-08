package com.namansuthar.games.widget.widget2048

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.cornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.Dp
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
import com.namansuthar.games.game2048.Game2048Engine
import com.namansuthar.games.game2048.model.Direction
import com.namansuthar.games.game2048.model.Game2048Action
import com.namansuthar.games.game2048.model.Game2048State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Glance widget for 2048 game.
 * Allows playing the game directly from the home screen.
 */
class Game2048Widget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            SMALL_SIZE,
            MEDIUM_SIZE,
            LARGE_SIZE
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current
            val widgetSize = determineWidgetSize(size)

            Game2048WidgetContent(
                glanceId = id,
                size = widgetSize
            )
        }
    }

    private fun determineWidgetSize(size: DpSize): WidgetSize {
        return when {
            size.width < 200.dp || size.height < 200.dp -> WidgetSize.SMALL
            size.width < 300.dp || size.height < 300.dp -> WidgetSize.MEDIUM
            else -> WidgetSize.LARGE
        }
    }

    companion object {
        private val SMALL_SIZE = DpSize(120.dp, 120.dp)
        private val MEDIUM_SIZE = DpSize(250.dp, 250.dp)
        private val LARGE_SIZE = DpSize(350.dp, 350.dp)
    }
}

enum class WidgetSize {
    SMALL, MEDIUM, LARGE
}

@Composable
fun Game2048WidgetContent(
    glanceId: GlanceId,
    size: WidgetSize
) {
    val context = LocalContext.current
    val widgetId = glanceId.toString()

    // Load state from repository
    val state = loadWidgetState(context, widgetId)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(android.graphics.Color.parseColor("#FAF8EF"))))
            .cornerRadius(16.dp)
            .padding(8.dp)
    ) {
        when (size) {
            WidgetSize.SMALL -> SmallWidgetLayout(state, widgetId)
            WidgetSize.MEDIUM -> MediumWidgetLayout(state, widgetId)
            WidgetSize.LARGE -> LargeWidgetLayout(state, widgetId)
        }
    }
}

@Composable
private fun SmallWidgetLayout(state: Game2048State, widgetId: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "2048",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        GameGrid(grid = state.grid, cellSize = 20.dp)
    }
}

@Composable
private fun MediumWidgetLayout(state: Game2048State, widgetId: String) {
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
                text = "2048",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "Score: ${state.score}",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Game Grid
        GameGrid(grid = state.grid, cellSize = 40.dp)

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Controls
        DirectionalControls(widgetId)
    }
}

@Composable
private fun LargeWidgetLayout(state: Game2048State, widgetId: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Top
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2048",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "SCORE",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
                Text(
                    text = state.score.toString(),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        // Game Grid
        GameGrid(grid = state.grid, cellSize = 60.dp)

        Spacer(modifier = GlanceModifier.height(12.dp))

        // Controls with action buttons
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DirectionalControls(widgetId)

            Spacer(modifier = GlanceModifier.height(8.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ActionButton(
                    text = "New",
                    action = actionRunCallback<NewGameAction>(
                        actionParametersOf(WIDGET_ID_KEY to widgetId)
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                ActionButton(
                    text = "Undo",
                    action = actionRunCallback<UndoAction>(
                        actionParametersOf(WIDGET_ID_KEY to widgetId)
                    ),
                    enabled = state.previousGrids.isNotEmpty()
                )
            }
        }
    }
}

@Composable
private fun GameGrid(grid: List<List<Int>>, cellSize: Dp) {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        grid.forEach { row ->
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                row.forEach { value ->
                    GameTile(value = value, size = cellSize)
                }
            }
        }
    }
}

@Composable
private fun GameTile(value: Int, size: Dp) {
    val backgroundColor = getTileColor(value)
    val textColor = if (value <= 4) {
        android.graphics.Color.parseColor("#776E65")
    } else {
        android.graphics.Color.WHITE
    }

    Box(
        modifier = GlanceModifier
            .size(size)
            .padding(2.dp)
            .background(ColorProvider(Color(backgroundColor)))
            .cornerRadius(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (value > 0) {
            Text(
                text = value.toString(),
                style = TextStyle(
                    fontSize = when {
                        value < 100 -> (size.value * 0.4f).sp
                        value < 1000 -> (size.value * 0.35f).sp
                        else -> (size.value * 0.3f).sp
                    },
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(textColor))
                )
            )
        }
    }
}

@Composable
private fun DirectionalControls(widgetId: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up button
        ControlButton(
            text = "↑",
            onClick = actionRunCallback<MoveAction>(
                actionParametersOf(
                    WIDGET_ID_KEY to widgetId,
                    DIRECTION_KEY to Direction.UP.name
                )
            )
        )

        Row {
            // Left button
            ControlButton(
                text = "←",
                onClick = actionRunCallback<MoveAction>(
                    actionParametersOf(
                        WIDGET_ID_KEY to widgetId,
                        DIRECTION_KEY to Direction.LEFT.name
                    )
                )
            )

            Spacer(modifier = GlanceModifier.width(60.dp))

            // Right button
            ControlButton(
                text = "→",
                onClick = actionRunCallback<MoveAction>(
                    actionParametersOf(
                        WIDGET_ID_KEY to widgetId,
                        DIRECTION_KEY to Direction.RIGHT.name
                    )
                )
            )
        }

        // Down button
        ControlButton(
            text = "↓",
            onClick = actionRunCallback<MoveAction>(
                actionParametersOf(
                    WIDGET_ID_KEY to widgetId,
                    DIRECTION_KEY to Direction.DOWN.name
                )
            )
        )
    }
}

@Composable
private fun ControlButton(
    text: String,
    onClick: androidx.glance.action.Action
) {
    Box(
        modifier = GlanceModifier
            .size(48.dp)
            .background(ColorProvider(Color(android.graphics.Color.parseColor("#8F7A66"))))
            .cornerRadius(8.dp)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color(android.graphics.Color.WHITE))
            )
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    action: androidx.glance.action.Action,
    enabled: Boolean = true
) {
    Box(
        modifier = GlanceModifier
            .width(80.dp)
            .height(40.dp)
            .background(
                ColorProvider(Color(
                    if (enabled) android.graphics.Color.parseColor("#8F7A66")
                    else android.graphics.Color.parseColor("#CDC1B4")
                ))
            )
            .cornerRadius(8.dp)
            .clickable(action),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color(android.graphics.Color.WHITE))
            )
        )
    }
}

private fun getTileColor(value: Int): Int {
    return when (value) {
        0 -> android.graphics.Color.parseColor("#CDC1B4")
        2 -> android.graphics.Color.parseColor("#EEE4DA")
        4 -> android.graphics.Color.parseColor("#EDE0C8")
        8 -> android.graphics.Color.parseColor("#F2B179")
        16 -> android.graphics.Color.parseColor("#F59563")
        32 -> android.graphics.Color.parseColor("#F67C5F")
        64 -> android.graphics.Color.parseColor("#F65E3B")
        128 -> android.graphics.Color.parseColor("#EDCF72")
        256 -> android.graphics.Color.parseColor("#EDCC61")
        512 -> android.graphics.Color.parseColor("#EDC850")
        1024 -> android.graphics.Color.parseColor("#EDC53F")
        2048 -> android.graphics.Color.parseColor("#EDC22E")
        else -> android.graphics.Color.parseColor("#3C3A32")
    }
}

@Composable
private fun loadWidgetState(context: Context, widgetId: String): Game2048State {
    // This is a placeholder - actual implementation will use repository
    return Game2048State.emptyGrid().let { grid ->
        Game2048State(grid = grid)
    }
}

// Action parameter keys
private val WIDGET_ID_KEY = ActionParameters.Key<String>("widget_id")
private val DIRECTION_KEY = ActionParameters.Key<String>("direction")

/**
 * Action callback for moving tiles.
 */
class MoveAction : ActionCallback, KoinComponent {
    private val engine: Game2048Engine by inject()
    private val repository: GameStateRepository by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val widgetId = parameters[WIDGET_ID_KEY] ?: return
        val directionName = parameters[DIRECTION_KEY] ?: return
        val direction = Direction.valueOf(directionName)

        withContext(Dispatchers.IO) {
            // Load current state
            val currentState = repository.getWidgetState(widgetId)?.let {
                engine.deserialize(it.stateData)
            } ?: engine.initialState()

            // Process move
            val result = engine.processAction(currentState, Game2048Action.Move(direction))

            if (result is GameResult.Success) {
                // Save new state
                repository.saveWidgetState(
                    widgetId = widgetId,
                    gameType = "2048",
                    stateData = engine.serialize(result.newState),
                    score = result.newState.score
                )

                // Update widget
                Game2048Widget().update(context, glanceId)
            }
        }
    }
}

/**
 * Action callback for starting a new game.
 */
class NewGameAction : ActionCallback, KoinComponent {
    private val engine: Game2048Engine by inject()
    private val repository: GameStateRepository by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val widgetId = parameters[WIDGET_ID_KEY] ?: return

        withContext(Dispatchers.IO) {
            val newState = engine.initialState()

            repository.saveWidgetState(
                widgetId = widgetId,
                gameType = "2048",
                stateData = engine.serialize(newState),
                score = 0
            )

            Game2048Widget().update(context, glanceId)
        }
    }
}

/**
 * Action callback for undoing the last move.
 */
class UndoAction : ActionCallback, KoinComponent {
    private val engine: Game2048Engine by inject()
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
            } ?: return@withContext

            val result = engine.processAction(currentState, Game2048Action.Undo)

            if (result is GameResult.Success) {
                repository.saveWidgetState(
                    widgetId = widgetId,
                    gameType = "2048",
                    stateData = engine.serialize(result.newState),
                    score = result.newState.score
                )

                Game2048Widget().update(context, glanceId)
            }
        }
    }
}
