package com.namansuthar.games.tictactoe.model

import com.namansuthar.games.engine.GameAction
import com.namansuthar.games.engine.GameState
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a player in the game.
 */
@Serializable
enum class Player {
    X, O, NONE;

    fun opponent(): Player = when (this) {
        X -> O
        O -> X
        NONE -> NONE
    }
}

/**
 * Game mode selection.
 */
@Serializable
enum class GameMode {
    PLAYER_VS_PLAYER,  // Two human players
    PLAYER_VS_CPU,     // Human vs AI
    CPU_VS_CPU         // Watch AI play itself
}

/**
 * Difficulty level for CPU opponent.
 */
@Serializable
enum class Difficulty {
    EASY,    // Random moves
    MEDIUM,  // Mix of random and optimal
    HARD     // Always optimal (minimax)
}

/**
 * Represents a position on the 3x3 grid.
 */
@Serializable
data class Position(val row: Int, val col: Int) {
    init {
        require(row in 0..2 && col in 0..2) { "Position must be in range 0..2" }
    }
}

/**
 * State of the Tic-Tac-Toe game.
 */
@Serializable
data class TicTacToeState(
    val grid: List<List<Player>> = List(3) { List(3) { Player.NONE } },
    val currentPlayer: Player = Player.X,
    val gameMode: GameMode = GameMode.PLAYER_VS_CPU,
    val difficulty: Difficulty = Difficulty.HARD,
    val winner: Player? = null,
    val winningLine: List<Position>? = null,
    val moveHistory: List<Position> = emptyList(),
    val xWins: Int = 0,
    val oWins: Int = 0,
    val draws: Int = 0,
    override val gameId: String = "tictactoe",
    override val timestamp: Long = System.currentTimeMillis()
) : GameState {

    val isGameOver: Boolean
        get() = winner != null || isBoardFull()

    val isDraw: Boolean
        get() = winner == null && isBoardFull()

    fun isBoardFull(): Boolean = grid.all { row -> row.all { it != Player.NONE } }

    fun isEmpty(): Boolean = grid.all { row -> row.all { it == Player.NONE } }

    fun getCell(position: Position): Player = grid[position.row][position.col]

    fun getCell(row: Int, col: Int): Player = grid[row][col]

    fun isCellEmpty(position: Position): Boolean = getCell(position) == Player.NONE

    fun getEmptyCells(): List<Position> {
        val emptyCells = mutableListOf<Position>()
        for (row in 0..2) {
            for (col in 0..2) {
                if (grid[row][col] == Player.NONE) {
                    emptyCells.add(Position(row, col))
                }
            }
        }
        return emptyCells
    }

    fun isCpuTurn(): Boolean {
        return when (gameMode) {
            GameMode.PLAYER_VS_CPU -> currentPlayer == Player.O
            GameMode.CPU_VS_CPU -> true
            GameMode.PLAYER_VS_PLAYER -> false
        }
    }
}

/**
 * Actions that can be performed in Tic-Tac-Toe.
 */
@Serializable
sealed class TicTacToeAction : GameAction {
    override val actionId: String = UUID.randomUUID().toString()
    override val timestamp: Long = System.currentTimeMillis()

    @Serializable
    data class PlaceMarker(val position: Position) : TicTacToeAction()

    @Serializable
    data class NewGame(
        val gameMode: GameMode = GameMode.PLAYER_VS_CPU,
        val difficulty: Difficulty = Difficulty.HARD
    ) : TicTacToeAction()

    @Serializable
    data object Undo : TicTacToeAction()

    @Serializable
    data object Restart : TicTacToeAction()
}
