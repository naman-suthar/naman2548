package com.namansuthar.games.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.namansuthar.games.core.data.database.dao.GameStateDao
import com.namansuthar.games.core.data.database.dao.WidgetStateDao
import com.namansuthar.games.core.data.database.entity.GameStateEntity
import com.namansuthar.games.core.data.database.entity.WidgetStateEntity

/**
 * Main Room database for the Board Games app.
 * Stores game states and widget states.
 */
@Database(
    entities = [
        GameStateEntity::class,
        WidgetStateEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameStateDao(): GameStateDao
    abstract fun widgetStateDao(): WidgetStateDao

    companion object {
        const val DATABASE_NAME = "board_games_db"
    }
}
