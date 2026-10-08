package com.namansuthar.games.snake.model

import com.namansuthar.games.engine.GameState
import kotlinx.serialization.Serializable

/**
 * State for a Snake game.
 *
 * @param snake List of positions representing the snake (head at index 0)
 * @param direction Current movement direction
 * @param food Position of the food
 * @param gridSize Size of the grid (default 12x12)
 * @param score Current score (increases when eating food)
 * @param speed Current speed level (1-5)
 * @param isPaused Whether the game is paused
 * @param isGameOver Whether the game is over
 * @param gameId Unique game identifier
 * @param timestamp Last update timestamp
 */
@Serializable
data class SnakeState(
    val snake: List<Position>,
    val direction: Direction,
    val food: Position,
    val gridSize: Int = DEFAULT_GRID_SIZE,
    val score: Int = 0,
    val speed: Int = 1,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    override val gameId: String = "snake",
    override val timestamp: Long = System.currentTimeMillis()
) : GameState {

    companion object {
        const val DEFAULT_GRID_SIZE = 12
        const val MAX_SPEED = 5

        /**
         * Get the delay in milliseconds for a given speed level.
         */
        fun getDelayForSpeed(speed: Int): Long {
            return when (speed) {
                1 -> 500L
                2 -> 350L
                3 -> 250L
                4 -> 150L
                5 -> 100L
                else -> 500L
            }
        }
    }

    /**
     * Get the head position of the snake.
     */
    val head: Position
        get() = snake.first()

    /**
     * Check if a position is occupied by the snake.
     */
    fun isSnakeAt(position: Position): Boolean {
        return snake.contains(position)
    }

    /**
     * Check if a position is the food.
     */
    fun isFoodAt(position: Position): Boolean {
        return food == position
    }
}

/**
 * Position on the grid.
 */
@Serializable
data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
}

/**
 * Direction of snake movement.
 */
@Serializable
enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    /**
     * Get the opposite direction.
     */
    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }

    /**
     * Get the movement vector for this direction.
     */
    fun toVector(): Position = when (this) {
        UP -> Position(0, -1)
        DOWN -> Position(0, 1)
        LEFT -> Position(-1, 0)
        RIGHT -> Position(1, 0)
    }
}
