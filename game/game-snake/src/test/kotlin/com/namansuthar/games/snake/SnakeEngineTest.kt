package com.namansuthar.games.snake

import com.google.common.truth.Truth.assertThat
import com.namansuthar.games.engine.GameEvent
import com.namansuthar.games.engine.GameResult
import com.namansuthar.games.snake.model.Direction
import com.namansuthar.games.snake.model.Position
import com.namansuthar.games.snake.model.SnakeAction
import com.namansuthar.games.snake.model.SnakeState
import org.junit.Before
import org.junit.Test

class SnakeEngineTest {

    private lateinit var engine: SnakeEngine

    @Before
    fun setup() {
        engine = SnakeEngine()
    }

    @Test
    fun `initial state creates snake with 3 segments`() {
        val state = engine.initialState()

        assertThat(state.snake).hasSize(3)
        assertThat(state.direction).isEqualTo(Direction.RIGHT)
        assertThat(state.score).isEqualTo(0)
        assertThat(state.isGameOver).isFalse()
    }

    @Test
    fun `move action advances snake in current direction`() {
        val state = SnakeState(
            snake = listOf(
                Position(5, 5),
                Position(4, 5),
                Position(3, 5)
            ),
            direction = Direction.RIGHT,
            food = Position(10, 10)
        )

        val result = engine.processAction(state, SnakeAction.Move)

        assertThat(result).isInstanceOf(GameResult.Success::class.java)
        val success = result as GameResult.Success
        assertThat(success.newState.head).isEqualTo(Position(6, 5))
        assertThat(success.newState.snake).hasSize(3)
    }

    @Test
    fun `snake grows when eating food`() {
        val state = SnakeState(
            snake = listOf(
                Position(5, 5),
                Position(4, 5),
                Position(3, 5)
            ),
            direction = Direction.RIGHT,
            food = Position(6, 5), // Food directly ahead
            score = 0
        )

        val result = engine.processAction(state, SnakeAction.Move) as GameResult.Success

        assertThat(result.newState.snake).hasSize(4)
        assertThat(result.newState.score).isEqualTo(10)
        assertThat(result.newState.food).isNotEqualTo(Position(6, 5)) // New food spawned
    }

    @Test
    fun `game over when hitting wall`() {
        val state = SnakeState(
            snake = listOf(
                Position(0, 5),
                Position(1, 5),
                Position(2, 5)
            ),
            direction = Direction.LEFT,
            food = Position(10, 10),
            gridSize = 12
        )

        val result = engine.processAction(state, SnakeAction.Move) as GameResult.Success

        assertThat(result.newState.isGameOver).isTrue()
        val gameOverEvent = result.events.filterIsInstance<GameEvent.GameOver>().first()
        assertThat(gameOverEvent.won).isFalse()
    }

    @Test
    fun `game over when hitting self`() {
        val state = SnakeState(
            snake = listOf(
                Position(5, 5),
                Position(4, 5),
                Position(4, 6),
                Position(5, 6)
            ),
            direction = Direction.DOWN,
            food = Position(10, 10)
        )

        val result = engine.processAction(state, SnakeAction.Move) as GameResult.Success

        assertThat(result.newState.isGameOver).isTrue()
    }

    @Test
    fun `cannot reverse direction into self`() {
        val state = SnakeState(
            snake = listOf(
                Position(5, 5),
                Position(4, 5),
                Position(3, 5)
            ),
            direction = Direction.RIGHT,
            food = Position(10, 10)
        )

        val result = engine.processAction(state, SnakeAction.ChangeDirection(Direction.LEFT))

        assertThat(result).isInstanceOf(GameResult.Invalid::class.java)
    }

    @Test
    fun `can change direction to perpendicular`() {
        val state = SnakeState(
            snake = listOf(
                Position(5, 5),
                Position(4, 5),
                Position(3, 5)
            ),
            direction = Direction.RIGHT,
            food = Position(10, 10)
        )

        val result = engine.processAction(state, SnakeAction.ChangeDirection(Direction.UP))

        assertThat(result).isInstanceOf(GameResult.Success::class.java)
        val success = result as GameResult.Success
        assertThat(success.newState.direction).isEqualTo(Direction.UP)
    }

    @Test
    fun `pause action sets isPaused to true`() {
        val state = engine.initialState()

        val result = engine.processAction(state, SnakeAction.Pause) as GameResult.Success

        assertThat(result.newState.isPaused).isTrue()
    }

    @Test
    fun `resume action sets isPaused to false`() {
        val state = engine.initialState().copy(isPaused = true)

        val result = engine.processAction(state, SnakeAction.Resume) as GameResult.Success

        assertThat(result.newState.isPaused).isFalse()
    }

    @Test
    fun `cannot move when paused`() {
        val state = engine.initialState().copy(isPaused = true)

        val result = engine.processAction(state, SnakeAction.Move)

        assertThat(result).isInstanceOf(GameResult.Invalid::class.java)
    }

    @Test
    fun `increase speed increments speed level`() {
        val state = engine.initialState().copy(speed = 2)

        val result = engine.processAction(state, SnakeAction.IncreaseSpeed) as GameResult.Success

        assertThat(result.newState.speed).isEqualTo(3)
    }

    @Test
    fun `decrease speed decrements speed level`() {
        val state = engine.initialState().copy(speed = 3)

        val result = engine.processAction(state, SnakeAction.DecreaseSpeed) as GameResult.Success

        assertThat(result.newState.speed).isEqualTo(2)
    }

    @Test
    fun `speed cannot exceed maximum`() {
        val state = engine.initialState().copy(speed = SnakeState.MAX_SPEED)

        val result = engine.processAction(state, SnakeAction.IncreaseSpeed)

        assertThat(result).isInstanceOf(GameResult.Invalid::class.java)
    }

    @Test
    fun `speed cannot go below 1`() {
        val state = engine.initialState().copy(speed = 1)

        val result = engine.processAction(state, SnakeAction.DecreaseSpeed)

        assertThat(result).isInstanceOf(GameResult.Invalid::class.java)
    }

    @Test
    fun `new game creates fresh state`() {
        val state = SnakeState(
            snake = listOf(Position(1, 1)),
            direction = Direction.DOWN,
            food = Position(5, 5),
            score = 100,
            speed = 5,
            isGameOver = true
        )

        val result = engine.processAction(state, SnakeAction.NewGame) as GameResult.Success

        assertThat(result.newState.score).isEqualTo(0)
        assertThat(result.newState.isGameOver).isFalse()
        assertThat(result.newState.speed).isEqualTo(1)
        assertThat(result.newState.snake).hasSize(3)
    }

    @Test
    fun `score increases by 10 when eating food`() {
        val state = SnakeState(
            snake = listOf(
                Position(5, 5),
                Position(4, 5)
            ),
            direction = Direction.RIGHT,
            food = Position(6, 5),
            score = 20
        )

        val result = engine.processAction(state, SnakeAction.Move) as GameResult.Success

        assertThat(result.newState.score).isEqualTo(30)

        val scoreEvent = result.events.filterIsInstance<GameEvent.ScoreChanged>().first()
        assertThat(scoreEvent.delta).isEqualTo(10)
    }

    @Test
    fun `serialize and deserialize round trip`() {
        val state = SnakeState(
            snake = listOf(
                Position(5, 5),
                Position(4, 5),
                Position(3, 5)
            ),
            direction = Direction.DOWN,
            food = Position(7, 8),
            score = 50,
            speed = 3
        )

        val serialized = engine.serialize(state)
        val deserialized = engine.deserialize(serialized)

        assertThat(deserialized.snake).isEqualTo(state.snake)
        assertThat(deserialized.direction).isEqualTo(state.direction)
        assertThat(deserialized.food).isEqualTo(state.food)
        assertThat(deserialized.score).isEqualTo(state.score)
        assertThat(deserialized.speed).isEqualTo(state.speed)
    }

    @Test
    fun `getScore returns current score`() {
        val state = engine.initialState().copy(score = 42)

        assertThat(engine.getScore(state)).isEqualTo(42)
    }

    @Test
    fun `direction opposite works correctly`() {
        assertThat(Direction.UP.opposite()).isEqualTo(Direction.DOWN)
        assertThat(Direction.DOWN.opposite()).isEqualTo(Direction.UP)
        assertThat(Direction.LEFT.opposite()).isEqualTo(Direction.RIGHT)
        assertThat(Direction.RIGHT.opposite()).isEqualTo(Direction.LEFT)
    }

    @Test
    fun `direction to vector works correctly`() {
        assertThat(Direction.UP.toVector()).isEqualTo(Position(0, -1))
        assertThat(Direction.DOWN.toVector()).isEqualTo(Position(0, 1))
        assertThat(Direction.LEFT.toVector()).isEqualTo(Position(-1, 0))
        assertThat(Direction.RIGHT.toVector()).isEqualTo(Position(1, 0))
    }
}
