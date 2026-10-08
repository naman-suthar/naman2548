package com.namansuthar.games.game2048

import com.namansuthar.games.engine.*
import com.namansuthar.games.game2048.model.Direction
import com.namansuthar.games.game2048.model.Game2048Action
import com.namansuthar.games.game2048.model.Game2048State
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Game engine for 2048.
 * Implements all game logic: movement, merging, spawning, win/loss detection.
 */
class Game2048Engine : GameEngine<Game2048State, Game2048Action> {

    private val json = Json { prettyPrint = false }

    override fun initialState(): Game2048State {
        val grid = Game2048State.emptyGrid()
        val withTile1 = addRandomTile(grid)
        val withTile2 = addRandomTile(withTile1)

        return Game2048State(grid = withTile2)
    }

    override fun processAction(
        state: Game2048State,
        action: Game2048Action
    ): GameResult<Game2048State> {
        return when (action) {
            is Game2048Action.Move -> processMove(state, action.direction)
            is Game2048Action.NewGame -> GameResult.Success(initialState())
            is Game2048Action.Undo -> processUndo(state)
        }
    }

    override fun isValidAction(state: Game2048State, action: Game2048Action): Boolean {
        return when (action) {
            is Game2048Action.Move -> !state.isGameOver && canMove(state.grid, action.direction)
            is Game2048Action.NewGame -> true
            is Game2048Action.Undo -> state.previousGrids.isNotEmpty()
        }
    }

    override fun isGameOver(state: Game2048State): Boolean {
        return state.isGameOver
    }

    override fun getScore(state: Game2048State): Int {
        return state.score
    }

    override fun serialize(state: Game2048State): String {
        return json.encodeToString(state)
    }

    override fun deserialize(data: String): Game2048State {
        return json.decodeFromString(data)
    }

    // Private helper methods

    private fun processMove(state: Game2048State, direction: Direction): GameResult<Game2048State> {
        if (!canMove(state.grid, direction)) {
            return GameResult.Invalid("No valid moves in that direction")
        }

        val (newGrid, scoreGained, merged) = moveAndMerge(state.grid, direction)
        val gridWithNewTile = addRandomTile(newGrid)
        val newScore = state.score + scoreGained

        val hasWon = !state.hasWon && gridWithNewTile.any { row ->
            row.any { it >= Game2048State.WIN_TILE }
        }

        val isGameOver = !hasAnyValidMoves(gridWithNewTile)

        // Save history for undo (limit to MAX_UNDO_HISTORY)
        val newHistory = (state.previousGrids + listOf(state.grid))
            .takeLast(Game2048State.MAX_UNDO_HISTORY)
        val newScoreHistory = (state.previousScores + listOf(state.score))
            .takeLast(Game2048State.MAX_UNDO_HISTORY)

        val events = buildList {
            add(GameEvent.ScoreChanged(newScore, scoreGained))
            merged.forEach { (pos, value) ->
                add(GameEvent.TileMerged(pos, value))
            }
            if (hasWon) {
                add(GameEvent.GameOver(won = true, finalScore = newScore))
            } else if (isGameOver) {
                add(GameEvent.GameOver(won = false, finalScore = newScore))
            }
        }

        return GameResult.Success(
            newState = state.copy(
                grid = gridWithNewTile,
                score = newScore,
                moves = state.moves + 1,
                previousGrids = newHistory,
                previousScores = newScoreHistory,
                isGameOver = isGameOver,
                hasWon = state.hasWon || hasWon,
                timestamp = currentTimestamp()
            ),
            events = events
        )
    }

    private fun processUndo(state: Game2048State): GameResult<Game2048State> {
        if (state.previousGrids.isEmpty()) {
            return GameResult.Invalid("No moves to undo")
        }

        val previousGrid = state.previousGrids.last()
        val previousScore = state.previousScores.last()

        return GameResult.Success(
            newState = state.copy(
                grid = previousGrid,
                score = previousScore,
                moves = maxOf(0, state.moves - 1),
                previousGrids = state.previousGrids.dropLast(1),
                previousScores = state.previousScores.dropLast(1),
                isGameOver = false,
                timestamp = currentTimestamp()
            )
        )
    }

    /**
     * Move and merge tiles in the specified direction.
     * Returns (newGrid, scoreGained, mergedPositions).
     */
    private fun moveAndMerge(
        grid: List<List<Int>>,
        direction: Direction
    ): Triple<List<List<Int>>, Int, List<Pair<Pair<Int, Int>, Int>>> {
        val rotated = rotateGrid(grid, direction)
        val (moved, score, merged) = mergeLeft(rotated)
        val restored = unrotateGrid(moved, direction)

        return Triple(restored, score, merged)
    }

    /**
     * Merge tiles moving left.
     * Returns (newGrid, scoreGained, mergedPositions).
     */
    private fun mergeLeft(
        grid: List<List<Int>>
    ): Triple<List<List<Int>>, Int, List<Pair<Pair<Int, Int>, Int>>> {
        var totalScore = 0
        val mergedTiles = mutableListOf<Pair<Pair<Int, Int>, Int>>()

        val newGrid = grid.mapIndexed { rowIndex, row ->
            val nonZero = row.filter { it != 0 }.toMutableList()
            val newRow = mutableListOf<Int>()
            var i = 0

            while (i < nonZero.size) {
                if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                    // Merge tiles
                    val mergedValue = nonZero[i] * 2
                    newRow.add(mergedValue)
                    totalScore += mergedValue
                    mergedTiles.add((rowIndex to newRow.size - 1) to mergedValue)
                    i += 2
                } else {
                    newRow.add(nonZero[i])
                    i++
                }
            }

            // Pad with zeros
            while (newRow.size < Game2048State.GRID_SIZE) {
                newRow.add(0)
            }

            newRow
        }

        return Triple(newGrid, totalScore, mergedTiles)
    }

    /**
     * Rotate grid to transform any direction into left movement.
     */
    private fun rotateGrid(grid: List<List<Int>>, direction: Direction): List<List<Int>> {
        return when (direction) {
            Direction.LEFT -> grid
            Direction.RIGHT -> grid.map { it.reversed() }
            Direction.UP -> transpose(grid)
            Direction.DOWN -> transpose(grid).map { it.reversed() }
        }
    }

    /**
     * Undo grid rotation after merging.
     */
    private fun unrotateGrid(grid: List<List<Int>>, direction: Direction): List<List<Int>> {
        return when (direction) {
            Direction.LEFT -> grid
            Direction.RIGHT -> grid.map { it.reversed() }
            Direction.UP -> transpose(grid)
            Direction.DOWN -> transpose(grid.map { it.reversed() })
        }
    }

    /**
     * Transpose a grid (swap rows and columns).
     */
    private fun transpose(grid: List<List<Int>>): List<List<Int>> {
        return List(grid.size) { col ->
            List(grid.size) { row ->
                grid[row][col]
            }
        }
    }

    /**
     * Check if a move in the given direction is possible.
     */
    private fun canMove(grid: List<List<Int>>, direction: Direction): Boolean {
        val rotated = rotateGrid(grid, direction)

        return rotated.any { row ->
            val nonZero = row.filter { it != 0 }

            // Can move if there are empty spaces before non-zero tiles
            if (row.indexOf(row.first { it != 0 }) > 0) return@any true

            // Can move if adjacent tiles can merge
            for (i in 0 until nonZero.size - 1) {
                if (nonZero[i] == nonZero[i + 1]) return@any true
            }

            false
        }
    }

    /**
     * Check if any valid moves exist.
     */
    private fun hasAnyValidMoves(grid: List<List<Int>>): Boolean {
        return Direction.entries.any { canMove(grid, it) }
    }

    /**
     * Add a random tile (2 or 4) to an empty cell.
     * 90% chance of 2, 10% chance of 4.
     */
    private fun addRandomTile(grid: List<List<Int>>): List<List<Int>> {
        val emptyCells = mutableListOf<Pair<Int, Int>>()

        for (row in grid.indices) {
            for (col in grid[row].indices) {
                if (grid[row][col] == 0) {
                    emptyCells.add(row to col)
                }
            }
        }

        if (emptyCells.isEmpty()) return grid

        val (row, col) = emptyCells.random()
        val value = if (Random.nextFloat() < 0.9f) 2 else 4

        return grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cell ->
                if (r == row && c == col) value else cell
            }
        }
    }
}
