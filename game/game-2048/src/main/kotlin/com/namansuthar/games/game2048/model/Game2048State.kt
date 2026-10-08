package com.namansuthar.games.game2048.model

import com.namansuthar.games.engine.GameState
import kotlinx.serialization.Serializable

/**
 * State for a 2048 game.
 *
 * @param grid 4x4 grid of tile values (0 = empty)
 * @param score Current score
 * @param moves Number of moves made
 * @param previousGrids History of previous grids for undo (max 10)
 * @param previousScores History of previous scores for undo
 * @param isGameOver Whether the game is over (no valid moves)
 * @param hasWon Whether the player has reached 2048
 * @param gameId Unique game identifier
 * @param timestamp Last update timestamp
 */
@Serializable
data class Game2048State(
    val grid: List<List<Int>>,
    val score: Int = 0,
    val moves: Int = 0,
    val previousGrids: List<List<List<Int>>> = emptyList(),
    val previousScores: List<Int> = emptyList(),
    val isGameOver: Boolean = false,
    val hasWon: Boolean = false,
    override val gameId: String = "2048",
    override val timestamp: Long = System.currentTimeMillis()
) : GameState {

    companion object {
        const val GRID_SIZE = 4
        const val WIN_TILE = 2048
        const val MAX_UNDO_HISTORY = 10

        /**
         * Create an empty 4x4 grid.
         */
        fun emptyGrid(): List<List<Int>> {
            return List(GRID_SIZE) { List(GRID_SIZE) { 0 } }
        }
    }

    /**
     * Get the value at a specific position.
     */
    fun getTile(row: Int, col: Int): Int {
        return grid.getOrNull(row)?.getOrNull(col) ?: 0
    }

    /**
     * Check if the grid is full (no empty cells).
     */
    fun isFull(): Boolean {
        return grid.all { row -> row.all { it != 0 } }
    }

    /**
     * Get all empty cell positions.
     */
    fun getEmptyCells(): List<Pair<Int, Int>> {
        return buildList {
            for (row in grid.indices) {
                for (col in grid[row].indices) {
                    if (grid[row][col] == 0) {
                        add(row to col)
                    }
                }
            }
        }
    }
}
