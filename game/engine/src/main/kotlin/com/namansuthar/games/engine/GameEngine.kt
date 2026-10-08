package com.namansuthar.games.engine

import java.util.UUID

/**
 * Base interface for all game states.
 * Each game implements its own state model.
 */
interface GameState {
    val gameId: String
    val timestamp: Long
}

/**
 * Base interface for all game actions.
 * Actions represent user interactions with the game.
 */
interface GameAction {
    val actionId: String
    val timestamp: Long
}

/**
 * Result of processing a game action.
 */
sealed class GameResult<out S : GameState> {
    /**
     * Action was processed successfully.
     * @param newState The updated game state
     * @param events Side effects from the action (score changes, game over, etc.)
     */
    data class Success<S : GameState>(
        val newState: S,
        val events: List<GameEvent> = emptyList()
    ) : GameResult<S>()

    /**
     * Action was invalid and not processed.
     * @param reason Human-readable explanation
     */
    data class Invalid(val reason: String) : GameResult<Nothing>()

    /**
     * An error occurred while processing the action.
     */
    data class Error(val exception: Throwable) : GameResult<Nothing>()
}

/**
 * Events emitted as side effects of game actions.
 * Used for triggering animations, sounds, haptics, etc.
 */
sealed interface GameEvent {
    data class ScoreChanged(val newScore: Int, val delta: Int) : GameEvent
    data class GameOver(val won: Boolean, val finalScore: Int) : GameEvent
    data class Achievement(val type: String, val value: Any) : GameEvent
    data class TileMerged(val position: Pair<Int, Int>, val value: Int) : GameEvent
    data class TileSpawned(val position: Pair<Int, Int>, val value: Int) : GameEvent
}

/**
 * Core game engine interface.
 * All games implement this to provide consistent game logic.
 *
 * @param S The game-specific state type
 * @param A The game-specific action type
 */
interface GameEngine<S : GameState, A : GameAction> {

    /**
     * Create a new game with initial state.
     */
    fun initialState(): S

    /**
     * Process a player action and return the new state.
     * This is a pure function - no side effects.
     */
    fun processAction(state: S, action: A): GameResult<S>

    /**
     * Check if an action is valid for the current state.
     * Used for UI feedback (e.g., graying out invalid moves).
     */
    fun isValidAction(state: S, action: A): Boolean

    /**
     * Check if the game is over.
     */
    fun isGameOver(state: S): Boolean

    /**
     * Get the current score.
     */
    fun getScore(state: S): Int

    /**
     * Serialize state to string for persistence.
     */
    fun serialize(state: S): String

    /**
     * Deserialize state from string.
     */
    fun deserialize(data: String): S
}

/**
 * Helper extension to create action IDs.
 */
fun generateActionId(): String = UUID.randomUUID().toString()

/**
 * Helper extension to get current timestamp.
 */
fun currentTimestamp(): Long = System.currentTimeMillis()
