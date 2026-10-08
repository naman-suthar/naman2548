package com.namansuthar.games.widget.snake

import android.content.Context
import androidx.compose.runtime.Composable
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
import androidx.work.*
import com.namansuthar.games.core.data.repository.GameStateRepository
import com.namansuthar.games.engine.GameResult
import com.namansuthar.games.snake.SnakeEngine
import com.namansuthar.games.snake.model.Direction
import com.namansuthar.games.snake.model.Position
import com.namansuthar.games.snake.model.SnakeAction
import com.namansuthar.games.snake.model.SnakeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * Glance widget for Snake game with auto-play functionality.
 */
class SnakeWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 180.dp),
            DpSize(250.dp, 250.dp),
            DpSize(300.dp, 300.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            SnakeWidgetContent(glanceId = id)
        }
    }

    companion object {
        private const val WORK_TAG_PREFIX = "snake_widget_"

        /**
         * Schedule auto-play updates for a widget.
         */
        fun scheduleAutoPlay(context: Context, widgetId: String, speed: Int) {
            val workManager = WorkManager.getInstance(context)
            val delay = SnakeState.getDelayForSpeed(speed)

            val workRequest = OneTimeWorkRequestBuilder<SnakeAutoPlayWorker>()
                .setInputData(
                    workDataOf(
                        "widgetId" to widgetId,
                        "delay" to delay
                    )
                )
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("$WORK_TAG_PREFIX$widgetId")
                .build()

            workManager.enqueueUniqueWork(
                "$WORK_TAG_PREFIX$widgetId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        /**
         * Cancel auto-play for a widget.
         */
        fun cancelAutoPlay(context: Context, widgetId: String) {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag("$WORK_TAG_PREFIX$widgetId")
        }
    }
}

@Composable
fun SnakeWidgetContent(glanceId: GlanceId) {
    val context = LocalContext.current
    val widgetId = glanceId.toString()

    // Load state (this would be from repository in production)
    val state = loadSnakeWidgetState(context, widgetId)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(android.graphics.Color.parseColor("#2C3E50")))
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with score
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Snake",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(android.graphics.Color.WHITE)
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "${state.score}",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(android.graphics.Color.parseColor("#2ECC71"))
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Game grid
            SnakeGameGrid(state = state)

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Controls
            if (!state.isGameOver) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pause/Resume button
                    ControlButton(
                        text = if (state.isPaused) "▶" else "⏸",
                        onClick = actionRunCallback<TogglePauseAction>(
                            actionParametersOf(WIDGET_ID_KEY to widgetId)
                        )
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    // Speed indicator
                    Text(
                        text = "${state.speed}x",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = ColorProvider(android.graphics.Color.WHITE)
                        )
                    )
                }
            } else {
                // New game button
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
private fun SnakeGameGrid(state: SnakeState) {
    val cellSize = 16.dp

    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (y in 0 until state.gridSize) {
            Row {
                for (x in 0 until state.gridSize) {
                    val position = Position(x, y)
                    SnakeGridCell(
                        isSnake = state.isSnakeAt(position),
                        isHead = state.head == position,
                        isFood = state.isFoodAt(position),
                        size = cellSize
                    )
                }
            }
        }
    }
}

@Composable
private fun SnakeGridCell(
    isSnake: Boolean,
    isHead: Boolean,
    isFood: Boolean,
    size: DpSize
) {
    val backgroundColor = when {
        isFood -> android.graphics.Color.parseColor("#E74C3C")
        isHead -> android.graphics.Color.parseColor("#2ECC71")
        isSnake -> android.graphics.Color.parseColor("#27AE60")
        else -> android.graphics.Color.parseColor("#34495E")
    }

    Box(
        modifier = GlanceModifier
            .size(size)
            .padding(0.5.dp)
            .background(ColorProvider(backgroundColor))
            .cornerRadius(2.dp)
    )
}

@Composable
private fun ControlButton(
    text: String,
    onClick: androidx.glance.action.Action
) {
    Box(
        modifier = GlanceModifier
            .height(36.dp)
            .padding(horizontal = 12.dp)
            .background(ColorProvider(android.graphics.Color.parseColor("#3498DB")))
            .cornerRadius(8.dp)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(android.graphics.Color.WHITE)
            )
        )
    }
}

@Composable
private fun loadSnakeWidgetState(context: Context, widgetId: String): SnakeState {
    // Placeholder - actual implementation uses repository
    return SnakeState(
        snake = listOf(
            Position(6, 6),
            Position(5, 6),
            Position(4, 6)
        ),
        direction = Direction.RIGHT,
        food = Position(9, 6)
    )
}

// Action parameter keys
private val WIDGET_ID_KEY = ActionParameters.Key<String>("widget_id")

/**
 * Toggle pause/resume state.
 */
class TogglePauseAction : ActionCallback, KoinComponent {
    private val engine: SnakeEngine by inject()
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

            val action = if (currentState.isPaused) {
                SnakeAction.Resume
            } else {
                SnakeAction.Pause
            }

            val result = engine.processAction(currentState, action)

            if (result is GameResult.Success) {
                repository.saveWidgetState(
                    widgetId = widgetId,
                    gameType = "snake",
                    stateData = engine.serialize(result.newState),
                    score = result.newState.score
                )

                // Schedule or cancel auto-play based on pause state
                if (result.newState.isPaused) {
                    SnakeWidget.cancelAutoPlay(context, widgetId)
                } else {
                    SnakeWidget.scheduleAutoPlay(context, widgetId, result.newState.speed)
                }

                SnakeWidget().update(context, glanceId)
            }
        }
    }
}

/**
 * Start a new game.
 */
class NewGameAction : ActionCallback, KoinComponent {
    private val engine: SnakeEngine by inject()
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
                gameType = "snake",
                stateData = engine.serialize(newState),
                score = 0
            )

            // Start auto-play
            SnakeWidget.scheduleAutoPlay(context, widgetId, newState.speed)
            SnakeWidget().update(context, glanceId)
        }
    }
}

/**
 * WorkManager worker for auto-playing the Snake game.
 */
class SnakeAutoPlayWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val engine: SnakeEngine by inject()
    private val repository: GameStateRepository by inject()

    override suspend fun doWork(): Result {
        val widgetId = inputData.getString("widgetId") ?: return Result.failure()

        return try {
            withContext(Dispatchers.IO) {
                val currentState = repository.getWidgetState(widgetId)?.let {
                    engine.deserialize(it.stateData)
                } ?: return@withContext Result.failure()

                // Don't move if paused or game over
                if (currentState.isPaused || currentState.isGameOver) {
                    return@withContext Result.success()
                }

                // Move the snake
                val result = engine.processAction(currentState, SnakeAction.Move)

                if (result is GameResult.Success) {
                    // Save new state
                    repository.saveWidgetState(
                        widgetId = widgetId,
                        gameType = "snake",
                        stateData = engine.serialize(result.newState),
                        score = result.newState.score
                    )

                    // Update widget UI
                    val glanceId = GlanceAppWidgetManager(applicationContext)
                        .getGlanceIds(SnakeWidgetReceiver::class.java)
                        .find { it.toString() == widgetId }

                    glanceId?.let {
                        SnakeWidget().update(applicationContext, it)
                    }

                    // Schedule next move if game is still active
                    if (!result.newState.isGameOver && !result.newState.isPaused) {
                        SnakeWidget.scheduleAutoPlay(
                            applicationContext,
                            widgetId,
                            result.newState.speed
                        )
                    }
                }

                Result.success()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
