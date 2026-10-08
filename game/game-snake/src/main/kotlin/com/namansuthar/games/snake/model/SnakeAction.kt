package com.namansuthar.games.snake.model

import com.namansuthar.games.engine.GameAction
import com.namansuthar.games.engine.currentTimestamp
import com.namansuthar.games.engine.generateActionId

/**
 * Actions that can be performed in a Snake game.
 */
sealed class SnakeAction(
    override val actionId: String = generateActionId(),
    override val timestamp: Long = currentTimestamp()
) : GameAction {

    /**
     * Change the snake's direction.
     */
    data class ChangeDirection(val newDirection: Direction) : SnakeAction()

    /**
     * Move the snake forward one step (auto-play).
     */
    data object Move : SnakeAction()

    /**
     * Pause the game.
     */
    data object Pause : SnakeAction()

    /**
     * Resume the game.
     */
    data object Resume : SnakeAction()

    /**
     * Start a new game.
     */
    data object NewGame : SnakeAction()

    /**
     * Increase speed level.
     */
    data object IncreaseSpeed : SnakeAction()

    /**
     * Decrease speed level.
     */
    data object DecreaseSpeed : SnakeAction()
}
