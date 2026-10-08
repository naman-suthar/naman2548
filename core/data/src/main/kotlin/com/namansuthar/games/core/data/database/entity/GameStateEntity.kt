package com.namansuthar.games.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing game states in the app.
 * Each game instance can save its state for later resumption.
 */
@Entity(tableName = "game_states")
data class GameStateEntity(
    @PrimaryKey
    val id: String,              // Composite: "${gameType}_${instanceId}"
    val gameType: String,         // "2048", "snake", "tictactoe"
    val instanceId: String,       // Unique instance identifier
    val stateData: String,        // Serialized game state (JSON)
    val score: Int,               // Current score
    val moves: Int,               // Number of moves made
    val isGameOver: Boolean,      // Whether game is finished
    val createdAt: Long,          // When the game was created
    val updatedAt: Long           // Last update timestamp
)
