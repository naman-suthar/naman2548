package com.namansuthar.games.tictactoe

import com.namansuthar.games.engine.GameEngine
import com.namansuthar.games.engine.GameEvent
import com.namansuthar.games.engine.GameResult
import com.namansuthar.games.tictactoe.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Game engine for Tic-Tac-Toe with minimax AI.
 */
class TicTacToeEngine : GameEngine<TicTacToeState, TicTacToeAction> {

    private val json = Json { ignoreUnknownKeys = true }

    override fun initialState(): TicTacToeState = TicTacToeState()

    override fun processAction(
        state: TicTacToeState,
        action: TicTacToeAction
    ): GameResult<TicTacToeState> {
        return try {
            when (action) {
                is TicTacToeAction.PlaceMarker -> processPlaceMarker(state, action.position)
                is TicTacToeAction.NewGame -> processNewGame(state, action.gameMode, action.difficulty)
                is TicTacToeAction.Undo -> processUndo(state)
                is TicTacToeAction.Restart -> GameResult.Success(initialState().copy(
                    gameMode = state.gameMode,
                    difficulty = state.difficulty,
                    xWins = state.xWins,
                    oWins = state.oWins,
                    draws = state.draws
                ))
            }
        } catch (e: Exception) {
            GameResult.Error(e)
        }
    }

    private fun processPlaceMarker(state: TicTacToeState, position: Position): GameResult<TicTacToeState> {
        if (state.isGameOver) {
            return GameResult.Invalid("Game is already over")
        }

        if (!state.isCellEmpty(position)) {
            return GameResult.Invalid("Cell is already occupied")
        }

        // Place marker
        val newGrid = state.grid.mapIndexed { rowIndex, row ->
            if (rowIndex == position.row) {
                row.mapIndexed { colIndex, cell ->
                    if (colIndex == position.col) state.currentPlayer else cell
                }
            } else {
                row
            }
        }

        val newMoveHistory = state.moveHistory + position

        // Check for winner
        val (winner, winningLine) = checkWinner(newGrid)

        // Update stats if game is over
        var newXWins = state.xWins
        var newOWins = state.oWins
        var newDraws = state.draws

        val events = mutableListOf<GameEvent>()

        if (winner != null) {
            when (winner) {
                Player.X -> {
                    newXWins++
                    events.add(GameEvent.Achievement("win", Player.X))
                }
                Player.O -> {
                    newOWins++
                    events.add(GameEvent.Achievement("win", Player.O))
                }
                Player.NONE -> {} // Should not happen
            }
        } else if (isBoardFull(newGrid)) {
            newDraws++
            events.add(GameEvent.Achievement("draw", newDraws))
        }

        val newState = state.copy(
            grid = newGrid,
            currentPlayer = state.currentPlayer.opponent(),
            winner = winner,
            winningLine = winningLine,
            moveHistory = newMoveHistory,
            xWins = newXWins,
            oWins = newOWins,
            draws = newDraws,
            timestamp = System.currentTimeMillis()
        )

        return GameResult.Success(newState, events)
    }

    private fun processNewGame(
        state: TicTacToeState,
        gameMode: GameMode,
        difficulty: Difficulty
    ): GameResult<TicTacToeState> {
        return GameResult.Success(
            initialState().copy(
                gameMode = gameMode,
                difficulty = difficulty,
                xWins = state.xWins,
                oWins = state.oWins,
                draws = state.draws
            )
        )
    }

    private fun processUndo(state: TicTacToeState): GameResult<TicTacToeState> {
        if (state.moveHistory.isEmpty()) {
            return GameResult.Invalid("No moves to undo")
        }

        // For player vs CPU, undo last 2 moves (player + CPU)
        val movesToUndo = if (state.gameMode == GameMode.PLAYER_VS_CPU) {
            minOf(2, state.moveHistory.size)
        } else {
            1
        }

        val newMoveHistory = state.moveHistory.dropLast(movesToUndo)

        // Rebuild grid from move history
        val newGrid = List(3) { List(3) { Player.NONE } }.toMutableList()
        var currentPlayer = Player.X

        newMoveHistory.forEach { position ->
            newGrid[position.row] = newGrid[position.row].toMutableList().apply {
                set(position.col, currentPlayer)
            }
            currentPlayer = currentPlayer.opponent()
        }

        return GameResult.Success(
            state.copy(
                grid = newGrid.map { it.toList() },
                currentPlayer = currentPlayer,
                winner = null,
                winningLine = null,
                moveHistory = newMoveHistory,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun checkWinner(grid: List<List<Player>>): Pair<Player?, List<Position>?> {
        // Check rows
        for (row in 0..2) {
            if (grid[row][0] != Player.NONE &&
                grid[row][0] == grid[row][1] &&
                grid[row][1] == grid[row][2]
            ) {
                return grid[row][0] to listOf(
                    Position(row, 0),
                    Position(row, 1),
                    Position(row, 2)
                )
            }
        }

        // Check columns
        for (col in 0..2) {
            if (grid[0][col] != Player.NONE &&
                grid[0][col] == grid[1][col] &&
                grid[1][col] == grid[2][col]
            ) {
                return grid[0][col] to listOf(
                    Position(0, col),
                    Position(1, col),
                    Position(2, col)
                )
            }
        }

        // Check diagonals
        if (grid[0][0] != Player.NONE &&
            grid[0][0] == grid[1][1] &&
            grid[1][1] == grid[2][2]
        ) {
            return grid[0][0] to listOf(
                Position(0, 0),
                Position(1, 1),
                Position(2, 2)
            )
        }

        if (grid[0][2] != Player.NONE &&
            grid[0][2] == grid[1][1] &&
            grid[1][1] == grid[2][0]
        ) {
            return grid[0][2] to listOf(
                Position(0, 2),
                Position(1, 1),
                Position(2, 0)
            )
        }

        return null to null
    }

    private fun isBoardFull(grid: List<List<Player>>): Boolean {
        return grid.all { row -> row.all { it != Player.NONE } }
    }

    override fun isValidAction(state: TicTacToeState, action: TicTacToeAction): Boolean {
        return when (action) {
            is TicTacToeAction.PlaceMarker -> {
                !state.isGameOver && state.isCellEmpty(action.position)
            }
            is TicTacToeAction.NewGame -> true
            is TicTacToeAction.Undo -> state.moveHistory.isNotEmpty()
            is TicTacToeAction.Restart -> true
        }
    }

    override fun isGameOver(state: TicTacToeState): Boolean = state.isGameOver

    override fun getScore(state: TicTacToeState): Int {
        return when (state.winner) {
            Player.X -> state.xWins
            Player.O -> state.oWins
            Player.NONE, null -> 0
        }
    }

    override fun serialize(state: TicTacToeState): String {
        return json.encodeToString(state)
    }

    override fun deserialize(data: String): TicTacToeState {
        return json.decodeFromString(data)
    }

    /**
     * Get the best move for the CPU using minimax algorithm.
     */
    fun getBestMove(state: TicTacToeState): Position? {
        if (state.isGameOver) return null

        return when (state.difficulty) {
            Difficulty.EASY -> getRandomMove(state)
            Difficulty.MEDIUM -> {
                // 50% chance of optimal move, 50% random
                if (Random.nextBoolean()) {
                    getMinimaxMove(state)
                } else {
                    getRandomMove(state)
                }
            }
            Difficulty.HARD -> getMinimaxMove(state)
        }
    }

    private fun getRandomMove(state: TicTacToeState): Position? {
        val emptyCells = state.getEmptyCells()
        return emptyCells.randomOrNull()
    }

    private fun getMinimaxMove(state: TicTacToeState): Position? {
        val emptyCells = state.getEmptyCells()
        if (emptyCells.isEmpty()) return null

        var bestScore = Int.MIN_VALUE
        var bestMove: Position? = null

        for (position in emptyCells) {
            val result = processAction(state, TicTacToeAction.PlaceMarker(position))
            if (result is GameResult.Success) {
                val score = minimax(result.newState, 0, false, Int.MIN_VALUE, Int.MAX_VALUE)
                if (score > bestScore) {
                    bestScore = score
                    bestMove = position
                }
            }
        }

        return bestMove
    }

    private fun minimax(
        state: TicTacToeState,
        depth: Int,
        isMaximizing: Boolean,
        alpha: Int,
        beta: Int
    ): Int {
        // Terminal states
        when (state.winner) {
            state.currentPlayer.opponent() -> return if (isMaximizing) -10 + depth else 10 - depth
            state.currentPlayer -> return if (isMaximizing) 10 - depth else -10 + depth
            else -> {}
        }

        if (state.isDraw) return 0

        var bestScore = if (isMaximizing) Int.MIN_VALUE else Int.MAX_VALUE
        var currentAlpha = alpha
        var currentBeta = beta

        for (position in state.getEmptyCells()) {
            val result = processAction(state, TicTacToeAction.PlaceMarker(position))
            if (result is GameResult.Success) {
                val score = minimax(result.newState, depth + 1, !isMaximizing, currentAlpha, currentBeta)

                if (isMaximizing) {
                    bestScore = maxOf(bestScore, score)
                    currentAlpha = maxOf(currentAlpha, score)
                } else {
                    bestScore = minOf(bestScore, score)
                    currentBeta = minOf(currentBeta, score)
                }

                // Alpha-beta pruning
                if (currentBeta <= currentAlpha) break
            }
        }

        return bestScore
    }
}
