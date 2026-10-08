package com.namansuthar.games.game2048

import com.google.common.truth.Truth.assertThat
import com.namansuthar.games.engine.GameEvent
import com.namansuthar.games.engine.GameResult
import com.namansuthar.games.game2048.model.Direction
import com.namansuthar.games.game2048.model.Game2048Action
import com.namansuthar.games.game2048.model.Game2048State
import org.junit.Before
import org.junit.Test

class Game2048EngineTest {

    private lateinit var engine: Game2048Engine

    @Before
    fun setup() {
        engine = Game2048Engine()
    }

    @Test
    fun `initial state creates 4x4 grid with two tiles`() {
        val state = engine.initialState()

        assertThat(state.grid).hasSize(4)
        assertThat(state.grid.all { it.size == 4 }).isTrue()

        val nonEmptyCells = state.grid.flatten().count { it != 0 }
        assertThat(nonEmptyCells).isEqualTo(2)
    }

    @Test
    fun `initial state has zero score and moves`() {
        val state = engine.initialState()

        assertThat(state.score).isEqualTo(0)
        assertThat(state.moves).isEqualTo(0)
        assertThat(state.isGameOver).isFalse()
        assertThat(state.hasWon).isFalse()
    }

    @Test
    fun `move left merges adjacent equal tiles`() {
        val state = Game2048State(
            grid = listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        val result = engine.processAction(state, Game2048Action.Move(Direction.LEFT))

        assertThat(result).isInstanceOf(GameResult.Success::class.java)
        val success = result as GameResult.Success
        assertThat(success.newState.grid[0][0]).isEqualTo(4)
        assertThat(success.newState.score).isEqualTo(4)
    }

    @Test
    fun `move right shifts tiles to right edge`() {
        val state = Game2048State(
            grid = listOf(
                listOf(2, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        val result = engine.processAction(state, Game2048Action.Move(Direction.RIGHT))

        val success = result as GameResult.Success
        assertThat(success.newState.grid[0][3]).isEqualTo(2)
        assertThat(success.newState.grid[0][0]).isEqualTo(0)
    }

    @Test
    fun `move up shifts tiles to top edge`() {
        val state = Game2048State(
            grid = listOf(
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(2, 0, 0, 0)
            )
        )

        val result = engine.processAction(state, Game2048Action.Move(Direction.UP))

        val success = result as GameResult.Success
        assertThat(success.newState.grid[0][0]).isEqualTo(2)
        assertThat(success.newState.grid[3][0]).isEqualTo(0)
    }

    @Test
    fun `move down shifts tiles to bottom edge`() {
        val state = Game2048State(
            grid = listOf(
                listOf(2, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        val result = engine.processAction(state, Game2048Action.Move(Direction.DOWN))

        val success = result as GameResult.Success
        assertThat(success.newState.grid[3][0]).isEqualTo(2)
        assertThat(success.newState.grid[0][0]).isEqualTo(0)
    }

    @Test
    fun `tiles merge only once per move`() {
        val state = Game2048State(
            grid = listOf(
                listOf(2, 2, 4, 4),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        val result = engine.processAction(state, Game2048Action.Move(Direction.LEFT))

        val success = result as GameResult.Success
        assertThat(success.newState.grid[0][0]).isEqualTo(4)
        assertThat(success.newState.grid[0][1]).isEqualTo(8)
        assertThat(success.newState.score).isEqualTo(12) // 4 + 8
    }

    @Test
    fun `score increases by merged tile values`() {
        val state = Game2048State(
            grid = listOf(
                listOf(4, 4, 0, 0),
                listOf(8, 8, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            ),
            score = 100
        )

        val result = engine.processAction(state, Game2048Action.Move(Direction.LEFT))

        val success = result as GameResult.Success
        // 4+4=8, 8+8=16, score gain = 8+16 = 24
        assertThat(success.newState.score).isEqualTo(124)
    }

    @Test
    fun `move count increments on valid move`() {
        val state = Game2048State(
            grid = listOf(
                listOf(2, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            ),
            moves = 5
        )

        val result = engine.processAction(state, Game2048Action.Move(Direction.RIGHT))

        val success = result as GameResult.Success
        assertThat(success.newState.moves).isEqualTo(6)
    }

    @Test
    fun `undo restores previous state`() {
        val initialGrid = listOf(
            listOf(2, 2, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )

        val state = Game2048State(
            grid = listOf(
                listOf(4, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            ),
            score = 4,
            moves = 1,
            previousGrids = listOf(initialGrid),
            previousScores = listOf(0)
        )

        val result = engine.processAction(state, Game2048Action.Undo)

        val success = result as GameResult.Success
        assertThat(success.newState.grid).isEqualTo(initialGrid)
        assertThat(success.newState.score).isEqualTo(0)
        assertThat(success.newState.moves).isEqualTo(0)
    }

    @Test
    fun `undo fails when no history exists`() {
        val state = Game2048State(
            grid = Game2048State.emptyGrid()
        )

        val result = engine.processAction(state, Game2048Action.Undo)

        assertThat(result).isInstanceOf(GameResult.Invalid::class.java)
    }

    @Test
    fun `new game action creates fresh state`() {
        val state = Game2048State(
            grid = listOf(
                listOf(2048, 1024, 512, 256),
                listOf(128, 64, 32, 16),
                listOf(8, 4, 2, 2),
                listOf(2, 2, 2, 2)
            ),
            score = 10000,
            moves = 500,
            isGameOver = true
        )

        val result = engine.processAction(state, Game2048Action.NewGame)

        val success = result as GameResult.Success
        assertThat(success.newState.score).isEqualTo(0)
        assertThat(success.newState.moves).isEqualTo(0)
        assertThat(success.newState.isGameOver).isFalse()

        val nonEmptyCells = success.newState.grid.flatten().count { it != 0 }
        assertThat(nonEmptyCells).isEqualTo(2)
    }

    @Test
    fun `hasWon becomes true when 2048 tile is created`() {
        val state = Game2048State(
            grid = listOf(
                listOf(1024, 1024, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        val result = engine.processAction(state, Game2048Action.Move(Direction.LEFT))

        val success = result as GameResult.Success
        assertThat(success.newState.hasWon).isTrue()

        // Check for GameOver event with won=true
        val gameOverEvent = success.events.filterIsInstance<GameEvent.GameOver>().firstOrNull()
        assertThat(gameOverEvent?.won).isTrue()
    }

    @Test
    fun `game is over when no valid moves exist`() {
        val state = Game2048State(
            grid = listOf(
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2),
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2)
            )
        )

        assertThat(engine.isGameOver(state)).isFalse() // Still processable

        // But isValidAction should return false for all directions
        assertThat(engine.isValidAction(state, Game2048Action.Move(Direction.UP))).isFalse()
        assertThat(engine.isValidAction(state, Game2048Action.Move(Direction.DOWN))).isFalse()
        assertThat(engine.isValidAction(state, Game2048Action.Move(Direction.LEFT))).isFalse()
        assertThat(engine.isValidAction(state, Game2048Action.Move(Direction.RIGHT))).isFalse()
    }

    @Test
    fun `isValidAction returns false when move would not change grid`() {
        val state = Game2048State(
            grid = listOf(
                listOf(2, 4, 8, 16),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        // Cannot move right - tiles are already on the right
        assertThat(engine.isValidAction(state, Game2048Action.Move(Direction.RIGHT))).isFalse()

        // Can move left, up, or down
        assertThat(engine.isValidAction(state, Game2048Action.Move(Direction.LEFT))).isTrue()
        assertThat(engine.isValidAction(state, Game2048Action.Move(Direction.UP))).isTrue()
        assertThat(engine.isValidAction(state, Game2048Action.Move(Direction.DOWN))).isTrue()
    }

    @Test
    fun `serialize and deserialize round trip`() {
        val state = Game2048State(
            grid = listOf(
                listOf(2, 4, 8, 16),
                listOf(32, 64, 128, 256),
                listOf(512, 1024, 2, 4),
                listOf(8, 16, 32, 64)
            ),
            score = 12345,
            moves = 67,
            hasWon = true
        )

        val serialized = engine.serialize(state)
        val deserialized = engine.deserialize(serialized)

        assertThat(deserialized.grid).isEqualTo(state.grid)
        assertThat(deserialized.score).isEqualTo(state.score)
        assertThat(deserialized.moves).isEqualTo(state.moves)
        assertThat(deserialized.hasWon).isEqualTo(state.hasWon)
    }

    @Test
    fun `move emits score changed event`() {
        val state = Game2048State(
            grid = listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        val result = engine.processAction(state, Game2048Action.Move(Direction.LEFT)) as GameResult.Success

        val scoreEvent = result.events.filterIsInstance<GameEvent.ScoreChanged>().first()
        assertThat(scoreEvent.newScore).isEqualTo(4)
        assertThat(scoreEvent.delta).isEqualTo(4)
    }

    @Test
    fun `getScore returns current score`() {
        val state = Game2048State(
            grid = Game2048State.emptyGrid(),
            score = 1234
        )

        assertThat(engine.getScore(state)).isEqualTo(1234)
    }
}
