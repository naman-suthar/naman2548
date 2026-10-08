package com.namansuthar.games.core.data.repository

import com.namansuthar.games.core.data.database.GameDatabase
import com.namansuthar.games.core.data.database.entity.GameStateEntity
import com.namansuthar.games.core.data.database.entity.WidgetStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing game and widget states.
 * Provides a clean API for data access across the app and widgets.
 */
class GameStateRepository(
    private val database: GameDatabase
) {

    // Game State operations

    suspend fun saveGameState(
        gameType: String,
        instanceId: String,
        stateData: String,
        score: Int,
        moves: Int,
        isGameOver: Boolean
    ) {
        val id = "${gameType}_${instanceId}"
        val now = System.currentTimeMillis()

        val existing = database.gameStateDao().getById(id)

        if (existing != null) {
            database.gameStateDao().update(
                existing.copy(
                    stateData = stateData,
                    score = score,
                    moves = moves,
                    isGameOver = isGameOver,
                    updatedAt = now
                )
            )
        } else {
            database.gameStateDao().insert(
                GameStateEntity(
                    id = id,
                    gameType = gameType,
                    instanceId = instanceId,
                    stateData = stateData,
                    score = score,
                    moves = moves,
                    isGameOver = isGameOver,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    suspend fun getGameState(gameType: String, instanceId: String): GameStateEntity? {
        val id = "${gameType}_${instanceId}"
        return database.gameStateDao().getById(id)
    }

    fun observeGamesByType(gameType: String): Flow<List<GameStateEntity>> {
        return database.gameStateDao().observeByGameType(gameType)
    }

    suspend fun deleteGameState(gameType: String, instanceId: String) {
        val id = "${gameType}_${instanceId}"
        database.gameStateDao().deleteById(id)
    }

    suspend fun deleteCompletedGames(gameType: String) {
        database.gameStateDao().deleteCompletedGames(gameType)
    }

    // Widget State operations

    suspend fun saveWidgetState(
        widgetId: String,
        gameType: String,
        stateData: String,
        score: Int
    ) {
        val now = System.currentTimeMillis()

        val existing = database.widgetStateDao().getByWidgetId(widgetId)

        if (existing != null) {
            database.widgetStateDao().update(
                existing.copy(
                    stateData = stateData,
                    score = score,
                    lastUpdate = now
                )
            )
        } else {
            database.widgetStateDao().insert(
                WidgetStateEntity(
                    widgetId = widgetId,
                    gameType = gameType,
                    stateData = stateData,
                    score = score,
                    lastUpdate = now
                )
            )
        }
    }

    suspend fun getWidgetState(widgetId: String): WidgetStateEntity? {
        return database.widgetStateDao().getByWidgetId(widgetId)
    }

    fun observeWidgetsByGameType(gameType: String): Flow<List<WidgetStateEntity>> {
        return database.widgetStateDao().observeByGameType(gameType)
    }

    fun observeAllWidgets(): Flow<List<WidgetStateEntity>> {
        return database.widgetStateDao().observeAll()
    }

    suspend fun deleteWidgetState(widgetId: String) {
        database.widgetStateDao().deleteByWidgetId(widgetId)
    }

    suspend fun updateWidgetPinnedStatus(widgetId: String, isPinned: Boolean) {
        database.widgetStateDao().updatePinnedStatus(widgetId, isPinned)
    }
}
