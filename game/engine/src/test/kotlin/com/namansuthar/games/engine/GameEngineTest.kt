package com.namansuthar.games.engine

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for game engine base contracts.
 */
class GameEngineTest {

    // Test implementation for testing purposes
    private data class TestGameState(
        override val gameId: String = "test",
        override val timestamp: Long = 0,
        val value: Int = 0
    ) : GameState

    private data class TestGameAction(
        override val actionId: String = "action",
        override val timestamp: Long = 0,
        val increment: Int = 1
    ) : GameAction

    private class TestGameEngine : GameEngine<TestGameState, TestGameAction> {
        override fun initialState() = TestGameState()

        override fun processAction(
            state: TestGameState,
            action: TestGameAction
        ): GameResult<TestGameState> {
            return GameResult.Success(
                state.copy(value = state.value + action.increment),
                listOf(GameEvent.ScoreChanged(state.value + action.increment, action.increment))
            )
        }

        override fun isValidAction(state: TestGameState, action: TestGameAction) = true
        override fun isGameOver(state: TestGameState) = state.value >= 100
        override fun getScore(state: TestGameState) = state.value
        override fun serialize(state: TestGameState) = state.value.toString()
        override fun deserialize(data: String) = TestGameState(value = data.toInt())
    }

    @Test
    fun `initial state creates default state`() {
        val engine = TestGameEngine()
        val state = engine.initialState()

        assertThat(state.value).isEqualTo(0)
    }

    @Test
    fun `processAction returns success with new state`() {
        val engine = TestGameEngine()
        val state = engine.initialState()
        val action = TestGameAction(increment = 5)

        val result = engine.processAction(state, action)

        assertThat(result).isInstanceOf(GameResult.Success::class.java)
        val success = result as GameResult.Success
        assertThat(success.newState.value).isEqualTo(5)
    }

    @Test
    fun `processAction emits score changed event`() {
        val engine = TestGameEngine()
        val state = engine.initialState()
        val action = TestGameAction(increment = 10)

        val result = engine.processAction(state, action) as GameResult.Success

        assertThat(result.events).hasSize(1)
        assertThat(result.events[0]).isInstanceOf(GameEvent.ScoreChanged::class.java)
    }

    @Test
    fun `isGameOver returns true when threshold reached`() {
        val engine = TestGameEngine()
        val state = TestGameState(value = 100)

        assertThat(engine.isGameOver(state)).isTrue()
    }

    @Test
    fun `isGameOver returns false when below threshold`() {
        val engine = TestGameEngine()
        val state = TestGameState(value = 50)

        assertThat(engine.isGameOver(state)).isFalse()
    }

    @Test
    fun `serialize and deserialize round trip`() {
        val engine = TestGameEngine()
        val state = TestGameState(value = 42)

        val serialized = engine.serialize(state)
        val deserialized = engine.deserialize(serialized)

        assertThat(deserialized.value).isEqualTo(42)
    }

    @Test
    fun `generateActionId creates unique IDs`() {
        val id1 = generateActionId()
        val id2 = generateActionId()

        assertThat(id1).isNotEqualTo(id2)
        assertThat(id1).isNotEmpty()
    }

    @Test
    fun `currentTimestamp returns valid timestamp`() {
        val before = System.currentTimeMillis()
        val timestamp = currentTimestamp()
        val after = System.currentTimeMillis()

        assertThat(timestamp).isAtLeast(before)
        assertThat(timestamp).isAtMost(after)
    }
}
