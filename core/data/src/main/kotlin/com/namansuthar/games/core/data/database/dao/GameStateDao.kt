package com.namansuthar.games.core.data.database.dao

import androidx.room.*
import com.namansuthar.games.core.data.database.entity.GameStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing game states in the database.
 */
@Dao
interface GameStateDao {

    @Query("SELECT * FROM game_states WHERE id = :id")
    suspend fun getById(id: String): GameStateEntity?

    @Query("SELECT * FROM game_states WHERE gameType = :gameType ORDER BY updatedAt DESC")
    fun observeByGameType(gameType: String): Flow<List<GameStateEntity>>

    @Query("SELECT * FROM game_states WHERE isGameOver = 0 ORDER BY updatedAt DESC")
    fun observeActivegames(): Flow<List<GameStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gameState: GameStateEntity)

    @Update
    suspend fun update(gameState: GameStateEntity)

    @Delete
    suspend fun delete(gameState: GameStateEntity)

    @Query("DELETE FROM game_states WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM game_states WHERE gameType = :gameType AND isGameOver = 1")
    suspend fun deleteCompletedGames(gameType: String)
}
