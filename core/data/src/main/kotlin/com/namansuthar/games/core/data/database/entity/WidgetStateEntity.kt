package com.namansuthar.games.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing widget states.
 * Each widget instance maintains its own game state.
 */
@Entity(tableName = "widget_states")
data class WidgetStateEntity(
    @PrimaryKey
    val widgetId: String,         // Android widget ID as string
    val gameType: String,         // "2048", "snake", "tictactoe"
    val stateData: String,        // Serialized game state (JSON)
    val score: Int,               // Current score
    val isPinned: Boolean = false, // User preference to keep this widget
    val lastUpdate: Long          // Last update timestamp
)
