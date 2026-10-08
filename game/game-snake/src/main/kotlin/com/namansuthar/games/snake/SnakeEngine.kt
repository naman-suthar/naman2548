package com.namansuthar.games.snake

import com.namansuthar.games.engine.*
import com.namansuthar.games.snake.model.Direction
import com.namansuthar.games.snake.model.Position
import com.namansuthar.games.snake.model.SnakeAction
import com.namansuthar.games.snake.model.SnakeState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Game engine for Snake.
 * Implements movement, collision detection, food spawning, and scoring.
 */
class SnakeEngine : GameEngine<SnakeState, SnakeAction> {

    private val json = Json { prettyPrint = false }

    override fun initialState(): SnakeState {
        val gridSize = SnakeState.DEFAULT_GRID_SIZE
        val center = gridSize / 2

        // Start with 3-segment snake in the center
        val snake = listOf(
            Position(center, center),
            Position(center - 1, center),
            Position(center - 2, center)
        )

        val food = spawnFood(snake, gridSize)

        return SnakeState(
            snake = snake,
            direction = Direction.RIGHT,
            food = food
        )
    }

    override fun processAction(
        state: SnakeState,
        action: SnakeAction
    ): GameResult<SnakeState> {
        return when (action) {
            is SnakeAction.ChangeDirection -> processChangeDirection(state, action.newDirection)
            is SnakeAction.Move -> processMove(state)
            is SnakeAction.Pause -> GameResult.Success(state.copy(isPaused = true))
            is SnakeAction.Resume -> GameResult.Success(state.copy(isPaused = false))
            is SnakeAction.NewGame -> GameResult.Success(initialState())
            is SnakeAction.IncreaseSpeed -> processSpeedChange(state, 1)
            is SnakeAction.DecreaseSpeed -> processSpeedChange(state, -1)
        }
    }

    override fun isValidAction(state: SnakeState, action: SnakeAction): Boolean {
        return when (action) {
            is SnakeAction.ChangeDirection -> {
                // Can't reverse direction into self
                action.newDirection != state.direction.opposite()
            }
            is SnakeAction.Move -> !state.isGameOver && !state.isPaused
            is SnakeAction.Pause -> !state.isPaused && !state.isGameOver
            is SnakeAction.Resume -> state.isPaused
            is SnakeAction.NewGame -> true
            is SnakeAction.IncreaseSpeed -> state.speed < SnakeState.MAX_SPEED
            is SnakeAction.DecreaseSpeed -> state.speed > 1
        }
    }

    override fun isGameOver(state: SnakeState): Boolean {
        return state.isGameOver
    }

    override fun getScore(state: SnakeState): Int {
        return state.score
    }

    override fun serialize(state: SnakeState): String {
        return json.encodeToString(state)
    }

    override fun deserialize(data: String): SnakeState {
        return json.decodeFromString(data)
    }

    // Private helper methods

    private fun processChangeDirection(
        state: SnakeState,
        newDirection: Direction
    ): GameResult<SnakeState> {
        // Prevent reversing into self
        if (newDirection == state.direction.opposite()) {
            return GameResult.Invalid("Cannot reverse direction")
        }

        return GameResult.Success(state.copy(direction = newDirection))
    }

    private fun processMove(state: SnakeState): GameResult<SnakeState> {
        if (state.isGameOver) {
            return GameResult.Invalid("Game is over")
        }

        if (state.isPaused) {
            return GameResult.Invalid("Game is paused")
        }

        // Calculate new head position
        val newHead = state.head + state.direction.toVector()

        // Check for wall collision
        if (isOutOfBounds(newHead, state.gridSize)) {
            return GameResult.Success(
                newState = state.copy(isGameOver = true),
                events = listOf(GameEvent.GameOver(won = false, finalScore = state.score))
            )
        }

        // Check for self collision
        if (state.snake.contains(newHead)) {
            return GameResult.Success(
                newState = state.copy(isGameOver = true),
                events = listOf(GameEvent.GameOver(won = false, finalScore = state.score))
            )
        }

        // Check if snake eats food
        val ateFood = newHead == state.food

        val newSnake = if (ateFood) {
            // Grow snake (don't remove tail)
            listOf(newHead) + state.snake
        } else {
            // Move snake (remove tail, add new head)
            listOf(newHead) + state.snake.dropLast(1)
        }

        val newScore = if (ateFood) state.score + 10 else state.score
        val newFood = if (ateFood) spawnFood(newSnake, state.gridSize) else state.food

        // Auto-increase speed every 50 points
        val newSpeed = if (ateFood && newScore % 50 == 0 && state.speed < SnakeState.MAX_SPEED) {
            state.speed + 1
        } else {
            state.speed
        }

        val events = buildList {
            if (ateFood) {
                add(GameEvent.ScoreChanged(newScore, 10))
                add(GameEvent.TileSpawned(newFood.x to newFood.y, 0))
                if (newSpeed > state.speed) {
                    add(GameEvent.Achievement("speed_up", newSpeed))
                }
            }
        }

        return GameResult.Success(
            newState = state.copy(
                snake = newSnake,
                food = newFood,
                score = newScore,
                speed = newSpeed,
                timestamp = currentTimestamp()
            ),
            events = events
        )
    }

    private fun processSpeedChange(state: SnakeState, delta: Int): GameResult<SnakeState> {
        val newSpeed = (state.speed + delta).coerceIn(1, SnakeState.MAX_SPEED)

        if (newSpeed == state.speed) {
            return GameResult.Invalid("Speed limit reached")
        }

        return GameResult.Success(state.copy(speed = newSpeed))
    }

    /**
     * Check if a position is out of bounds.
     */
    private fun isOutOfBounds(position: Position, gridSize: Int): Boolean {
        return position.x < 0 || position.x >= gridSize ||
               position.y < 0 || position.y >= gridSize
    }

    /**
     * Spawn food at a random empty position.
     */
    private fun spawnFood(snake: List<Position>, gridSize: Int): Position {
        val emptyPositions = mutableListOf<Position>()

        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                val pos = Position(x, y)
                if (!snake.contains(pos)) {
                    emptyPositions.add(pos)
                }
            }
        }

        return if (emptyPositions.isNotEmpty()) {
            emptyPositions[Random.nextInt(emptyPositions.size)]
        } else {
            // Grid is full (shouldn't happen in practice)
            Position(0, 0)
        }
    }
}
