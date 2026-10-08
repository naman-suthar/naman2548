package com.namansuthar.games.game2048.model

import com.namansuthar.games.engine.GameAction
import com.namansuthar.games.engine.currentTimestamp
import com.namansuthar.games.engine.generateActionId

/**
 * Actions that can be performed in a 2048 game.
 */
sealed class Game2048Action(
    override val actionId: String = generateActionId(),
    override val timestamp: Long = currentTimestamp()
) : GameAction {

    /**
     * Move tiles in a direction.
     */
    data class Move(val direction: Direction) : Game2048Action()

    /**
     * Start a new game.
     */
    data object NewGame : Game2048Action()

    /**
     * Undo the last move.
     */
    data object Undo : Game2048Action()
}

/**
 * Direction for tile movement.
 */
enum class Direction {
    UP, DOWN, LEFT, RIGHT
}
